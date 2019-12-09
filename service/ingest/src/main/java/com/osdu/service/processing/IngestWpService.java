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

import com.google.common.collect.ImmutableMap;
import com.networknt.schema.ValidationMessage;
import com.osdu.client.delfi.RecordDataFields;
import com.osdu.model.Record;
import com.osdu.model.RequestContext;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.ingest.IngestedWp;
import com.osdu.model.ingest.IngestedWpc;
import com.osdu.model.type.manifest.ManifestWp;
import com.osdu.model.type.manifest.ManifestWpc;
import com.osdu.model.type.wp.WorkProduct;
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
public class IngestWpService {

  final SrnMappingService srnMappingService;
  final DelfiPortalService delfiPortalService;
  final JsonValidationService jsonValidationService;
  final IngestWpcService ingestWpcService;

  /**
   * Process manifest work product. It includes ingestion manifest work product components
   * and creating a record for wp.
   * @param wp manifest work product
   * @param requestContext ingest request context
   * @return ingested wp
   */
  public IngestedWp processWp(ManifestWp wp, RequestContext requestContext) {
    log.debug("Start process work product.");

    IngestedWp ingestedWp;
    try {
      ingestedWp = ingestWp(wp, requestContext).toBuilder()
          .summary("Wp is processed")
          .build();
    } catch (Exception e) {
      String failedMsg = "Exception is happened during ingesting wp.";
      log.error(failedMsg, e);
      ingestedWp = IngestedWp.builder()
          .success(false)
          .summary(failedMsg)
          .build();
    }

    log.debug("Work product is processed: {}", ingestedWp);
    return ingestedWp;
  }

  private IngestedWp ingestWp(ManifestWp wp, RequestContext requestContext) {
    final SchemaData wpSchemaData = srnMappingService.getSchemaData(wp.getResourceTypeID());

    List<IngestedWpc> ingestedWpcs = getWpcStream(wp)
        .map(wpc -> ingestWpcService.processWpc(wpc, requestContext))
        .collect(Collectors.toList());

    List<String> wpcSrns = new ArrayList<>(ingestedWpcs.size());
    List<String> summaries = new ArrayList<>(ingestedWpcs.size() + 1);
    boolean successWpcs = true;

    for (IngestedWpc wpc: ingestedWpcs) {
      successWpcs &= wpc.isSuccess();
      wpcSrns.add(wpc.getSrn());
      summaries.add(String.join(System.lineSeparator(), wpc.getSummaries()));
    }

    // generate srn
    String wpSrn = generateSrn(new ResourceTypeId(wp.getResourceTypeID()));

    // create wp record
    Record wpRecord = createWpRecord(wp, wpSrn, wpcSrns, wpSchemaData, requestContext);

    // validate wp record
    Set<ValidationMessage> errors = validateWpRecord(wpRecord, wpSchemaData);
    boolean validWp = errors.isEmpty();

    if (!validWp) {
      String validationMsg = errors.stream()
          .map(ValidationMessage::getMessage)
          .collect(Collectors.joining(System.lineSeparator()));
      summaries.add("Wp record is invalid. Validation message: " + validationMsg);
    }

    return IngestedWp.builder()
        .ingestedWpcs(ingestedWpcs)
        .srn(wpSrn)
        .wpRecord(wpRecord)
        .success(successWpcs && validWp)
        .summaries(summaries)
        .build();
  }

  private Stream<ManifestWpc> getWpcStream(ManifestWp wp) {
    List<ManifestWpc> wpcs = wp.getManifestWpcs();
    if (wpcs.size() > 1) {
      return wpcs.parallelStream();
    }

    return wpcs.stream();
  }

  private Record createWpRecord(ManifestWp wp, String wpSrn, List<String> wpcSrns,
      SchemaData schemaData, RequestContext requestContext) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    WorkProduct newWp = deepCopy(wp, WorkProduct.class);

    newWp.setResourceID(wpSrn);
    newWp.setResourceTypeID(IngestionHelper.prepareTypeId(wp.getResourceTypeID()));
    newWp.setResourceHomeRegionID(requestContext.getHeaders().getResourceHomeRegionID());
    newWp.setResourceHostRegionIDs(requestContext.getHeaders().getResourceHostRegionIDs());
    newWp.setResourceObjectCreationDatetime(now);
    newWp.setResourceVersionCreationDatetime(now);
    newWp.setResourceCurationStatus("srn:reference-data/ResourceCurationStatus:CREATED:");
    newWp.setResourceLifecycleStatus("srn:reference-data/ResourceLifecycleStatus:RECIEVED:");
    newWp.getData().getGroupTypeProperties().setComponents(wpcSrns);

    DelfiRecord delfiRecord = DelfiRecord.builder()
        .kind(schemaData.getKind())
        .acl(getAcl(requestContext.getUserGroupEmailByName()))
        .legal(requestContext.getHeaders().getLegalTagsObject().getLegal())
        .data(ImmutableMap.of(RecordDataFields.OSDU_DATA, newWp))
        .build();

    Record record = delfiPortalService.putRecord(delfiRecord,
        requestContext.getAuthorizationToken(), requestContext.getPartition());

    srnMappingService.saveSrnToRecord(SrnToRecord.builder()
        .recordId(record.getId())
        .srn(wpSrn)
        .build());

    return record;
  }

  private Set<ValidationMessage> validateWpRecord(Record wpRecord, SchemaData schemaData) {
    return jsonValidationService.validate(schemaData.getSchema(),
        JsonUtils.getJsonNode(wpRecord.getData().get(RecordDataFields.OSDU_DATA)));
  }

}
