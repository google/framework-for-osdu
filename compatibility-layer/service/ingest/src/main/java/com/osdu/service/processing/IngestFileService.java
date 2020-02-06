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

import static com.osdu.service.JsonUtils.getJsonNode;
import static com.osdu.service.helper.IngestionHelper.generateSrn;
import static java.lang.String.format;

import com.networknt.schema.ValidationMessage;
import com.osdu.client.delfi.RecordDataFields;
import com.osdu.model.RequestContext;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiIngestedFile;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.status.JobInfo;
import com.osdu.model.delfi.status.JobPollingResult;
import com.osdu.model.delfi.status.MasterJobStatus;
import com.osdu.model.delfi.submit.SubmitFileContext;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.ingest.IngestedFile;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.service.EnrichService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.SubmitService;
import com.osdu.service.delfi.DelfiIngestionService;
import com.osdu.service.validation.JsonValidationService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestFileService {

  final DelfiIngestionService delfiIngestionService;
  final SubmitService submitService;
  final EnrichService enrichService;
  final JsonValidationService jsonValidationService;
  final SrnMappingService srnMappingService;

  /**
   * Process manifest file. Trying to ingest file otherwise saving an exception message to summary.
   * @param file manifest file
   * @param requestContext ingest request context
   * @return ingested file
   */
  public IngestedFile processFile(ManifestFile file,
      RequestContext requestContext) {
    String logicalPath = format("wpc(id: %s).file(id: %s)",
        file.getWpc().getAssociativeId(), file.getAssociativeId());
    log.debug("Start process manifest file (path = {})", logicalPath);
    IngestedFile ingestedFile;

    try {
      ingestedFile = ingestFile(file, requestContext).toBuilder()
          .summary(format("File (path: %s) is processed.",
              logicalPath))
          .build();
    } catch (Exception e) {
      String failedMsg = format("Exception is happen during ingesting file (path = %s).",
          logicalPath);
      log.error(failedMsg, e);
      ingestedFile = IngestedFile.builder()
          .success(false)
          .summary(failedMsg)
          .build();
    }

    log.debug("Manifest file (path = {}) is processed: {}", logicalPath, ingestedFile);
    return ingestedFile;
  }

  private IngestedFile ingestFile(ManifestFile file, RequestContext requestContext) {
    String logicalPath = format("wpc(id: %s).file(id: %s)",
        file.getWpc().getAssociativeId(), file.getAssociativeId());
    SchemaData schemaData = srnMappingService.getSchemaData(file.getResourceTypeID());

    // signed url
    SignedFile signedFile = delfiIngestionService
        .uploadFile(file, requestContext.getAuthorizationToken(), requestContext.getPartition());
    // submit file
    SubmittedFile submittedFile = submitFile(signedFile, schemaData, requestContext);

    // await job
    log.debug("Waited for submitted ingestion job to be finished. JobId: {}",
        submittedFile.getJobId());
    JobPollingResult jobPollingResult = submitService
        .awaitSubmitJob(submittedFile.getJobId(), requestContext);
    log.debug("Job polling result: {}", jobPollingResult);

    if (jobPollingResult.getStatus() != MasterJobStatus.COMPLETED) {
      JobInfo jobInfo = jobPollingResult.getJob().getJobInfo();
      String failedMsg = format("Failed to ingest file: %s, path: %s, job status: %s",
          jobInfo.getFileName(), logicalPath, jobInfo.getCurrentJobStatus());
      return IngestedFile.builder()
          .success(false)
          .summary(failedMsg)
          .build();
    } else {

      DelfiIngestedFile delfiIngestedFile = submitService
          .getIngestedFile(submittedFile, jobPollingResult.getJob(), requestContext);
      log.debug("Delfi ingested files: {}", delfiIngestedFile);

      // generate srn
      String srn = generateSrn(new ResourceTypeId(file.getResourceTypeID()));

      // enrich
      EnrichedFile enrichedFile = enrichService
          .enrichRecord(delfiIngestedFile, srn, requestContext);

      // validate
      Set<ValidationMessage> validationErrors = validateFile(enrichedFile, schemaData);

      // create SRN-record mapping
      srnMappingService.saveSrnToRecord(SrnToRecord.builder()
          .recordId(enrichedFile.getRecord().getId())
          .srn(srn)
          .build());

      return IngestedFile.builder()
          .record(enrichedFile.getRecord())
          .srn(srn)
          .success(validationErrors.isEmpty())
          .summary(getIngestSummary(validationErrors, logicalPath))
          .build();
    }
  }

  private SubmittedFile submitFile(SignedFile file, SchemaData schemaData,
      RequestContext requestContext) {
    SubmitFileContext fileContext = SubmitFileContext.builder()
        .relativeFilePath(file.getRelativeFilePath())
        .kind(schemaData.getKind())
        .wpcResourceTypeId(file.getFile().getWpc().getResourceTypeID())
        .fileResourceTypeId(file.getFile().getResourceTypeID())
        .build();
    SubmitJobResult result = submitService.submitFile(fileContext, requestContext);

    return SubmittedFile.builder()
        .signedFile(file)
        .jobId(result.getJobId())
        .build();
  }

  private Set<ValidationMessage> validateFile(EnrichedFile file, SchemaData schemaData) {
    return jsonValidationService.validate(
        schemaData.getSchema(), getJsonNode(
            file.getRecord().getData().get(RecordDataFields.OSDU_DATA)));
  }

  private String getIngestSummary(Set<ValidationMessage> errors, String logicalPath) {
    if (errors.isEmpty()) {
      return format("File record is valid. Path: %s", logicalPath);
    }

    String validationMsg = errors.stream()
        .map(ValidationMessage::getMessage)
        .collect(Collectors.joining(System.lineSeparator()));
    return format("File record is invalid. Path: %s, Validation message: %s",
        logicalPath, validationMsg);
  }

}
