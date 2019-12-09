/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.service.processing;

import static com.osdu.service.JsonUtils.deepCopy;
import static com.osdu.service.helper.IngestionHelper.generateSrn;
import static com.osdu.service.helper.IngestionHelper.getAcl;
import static java.lang.String.format;

import com.google.common.collect.ImmutableMap;
import com.networknt.schema.ValidationMessage;
import com.osdu.client.delfi.RecordDataFields;
import com.osdu.model.Record;
import com.osdu.model.RequestContext;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.ingest.IngestedFile;
import com.osdu.model.ingest.IngestedWpc;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.model.type.manifest.ManifestWpc;
import com.osdu.model.type.wp.WorkProductComponent;
import com.osdu.service.JsonUtils;
import com.osdu.service.SrnMappingService;
import com.osdu.service.delfi.DelfiPortalService;
import com.osdu.service.helper.IngestionHelper;
import com.osdu.service.validation.JsonValidationService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestWpcService {

  final IngestFileService ingestFileService;
  final SrnMappingService srnMappingService;
  final DelfiPortalService delfiPortalService;
  final JsonValidationService jsonValidationService;

  /**
   * Process manifest work product component. It includes ingestion manifest files
   * and creating a record for wpc.
   * @param wpc manifest work product component
   * @param requestContext ingest request context
   * @return ingested wpc
   */
  public IngestedWpc processWpc(ManifestWpc wpc, RequestContext requestContext) {
    String logicalPath = wpc.getAssociativeId();
    log.debug("Start process work product component (path = {}).", logicalPath);

    IngestedWpc ingestedWpc;
    try {
      ingestedWpc = ingestWpc(wpc, requestContext).toBuilder()
          .summary(format("Wpc (path = %s) is processed", logicalPath))
          .build();
    } catch (Exception e) {
      String failedMsg = format("Exception is happened during ingesting wpc (path = %s).",
          logicalPath);
      log.error(failedMsg, e);
      ingestedWpc = IngestedWpc.builder()
          .success(false)
          .summary(failedMsg)
          .build();
    }

    log.debug("Work product component is processed: {}", ingestedWpc);
    return ingestedWpc;
  }

  private IngestedWpc ingestWpc(ManifestWpc wpc, RequestContext requestContext) {
    final SchemaData wpcSchemaData = srnMappingService.getSchemaData(wpc.getResourceTypeID());

    // process files
    List<IngestedFile> ingestedFiles = getFileStream(wpc)
        .map(file -> ingestFileService.processFile(file, requestContext))
        .collect(Collectors.toList());

    // analyse file ingestion result
    List<String> fileSrns = new ArrayList<>(ingestedFiles.size());
    List<String> summaries = new ArrayList<>(ingestedFiles.size() + 1);
    boolean successFiles = true;

    for (IngestedFile file: ingestedFiles) {
      successFiles &= file.isSuccess();
      fileSrns.add(file.getSrn());
      summaries.add(String.join(System.lineSeparator(), file.getSummaries()));
    }

    // generate wpc srn
    String wpcSrn = generateSrn(new ResourceTypeId(wpc.getResourceTypeID()));

    // create wpc record
    Record wpcRecord = createWpcRecord(wpc, wpcSrn, fileSrns, wpcSchemaData, requestContext);

    // validate wpc record
    Set<ValidationMessage> errors = validateWpcRecord(wpcRecord, wpcSchemaData);
    boolean validWpc = errors.isEmpty();

    if (!validWpc) {
      String validationMsg = errors.stream()
          .map(ValidationMessage::getMessage)
          .collect(Collectors.joining(System.lineSeparator()));
      summaries.add(format("Wpc(ID: %s) record is invalid. Validation message: %s",
          wpc.getAssociativeId(), validationMsg));
    }

    return IngestedWpc.builder()
        .ingestedFiles(ingestedFiles)
        .srn(wpcSrn)
        .wpcRecord(wpcRecord)
        .success(successFiles && validWpc)
        .summaries(summaries)
        .build();
  }

  private Stream<ManifestFile> getFileStream(ManifestWpc wpc) {
    List<ManifestFile> files = wpc.getFiles();
    if (files.size() > 1) {
      return files.parallelStream();
    }

    return files.stream();
  }

  private Record createWpcRecord(ManifestWpc wpc, String wpcSrn, List<String> fileSrns,
      SchemaData schemaData, RequestContext requestContext) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    WorkProductComponent newWpc = deepCopy(wpc, WorkProductComponent.class);

    newWpc.setResourceID(wpcSrn);
    newWpc.setResourceTypeID(IngestionHelper.prepareTypeId(wpc.getResourceTypeID()));
    newWpc.setResourceHomeRegionID(requestContext.getHeaders().getResourceHomeRegionID());
    newWpc.setResourceHostRegionIDs(requestContext.getHeaders().getResourceHostRegionIDs());
    newWpc.setResourceObjectCreationDatetime(now);
    newWpc.setResourceVersionCreationDatetime(now);
    newWpc.setResourceCurationStatus("srn:reference-data/ResourceCurationStatus:CREATED:");
    newWpc.setResourceLifecycleStatus("srn:reference-data/ResourceLifecycleStatus:RECIEVED:");
    newWpc.getData().getGroupTypeProperties().setFiles(fileSrns);

    DelfiRecord delfiRecord = DelfiRecord.builder()
        .kind(schemaData.getKind())
        .acl(getAcl(requestContext.getUserGroupEmailByName()))
        .legal(requestContext.getHeaders().getLegalTagsObject().getLegal())
        .data(ImmutableMap.of(RecordDataFields.OSDU_DATA, newWpc))
        .build();

    Record record = delfiPortalService.putRecord(delfiRecord,
        requestContext.getAuthorizationToken(), requestContext.getPartition());

    srnMappingService.saveSrnToRecord(SrnToRecord.builder()
        .recordId(record.getId())
        .srn(wpcSrn)
        .build());

    return record;
  }

  private Set<ValidationMessage> validateWpcRecord(Record wpcRecord, SchemaData schemaData) {
    return jsonValidationService.validate(schemaData.getSchema(),
        JsonUtils.getJsonNode(wpcRecord.getData().get(RecordDataFields.OSDU_DATA)));
  }

}
