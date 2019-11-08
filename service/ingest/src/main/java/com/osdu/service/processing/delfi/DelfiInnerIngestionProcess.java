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

package com.osdu.service.processing.delfi;

import static com.osdu.model.job.IngestJobStatus.COMPLETE;
import static com.osdu.model.job.IngestJobStatus.FAILED;
import static com.osdu.service.JsonUtils.getJsonNode;
import static com.osdu.service.helper.IngestionHelper.normalizePartition;

import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.model.IngestHeaders;
import com.osdu.model.Record;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.delfi.entitlement.Group;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.status.JobsPullingResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.job.InnerIngestResult;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.service.EnrichService;
import com.osdu.service.JobStatusService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.SubmitService;
import com.osdu.service.delfi.DelfiIngestionService;
import com.osdu.service.helper.IngestionHelper;
import com.osdu.service.processing.InnerIngestionProcess;
import com.osdu.service.validation.JsonValidationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelfiInnerIngestionProcess implements InnerIngestionProcess {

  final DelfiPortalProperties portalProperties;
  final DelfiEntitlementsClient delfiEntitlementsClient;
  final SrnMappingService srnMappingService;
  final EnrichService enrichService;
  final SubmitService submitService;
  final JobStatusService jobStatusService;
  final JsonValidationService jsonValidationService;
  final DelfiIngestionService delfiIngestionService;
  final IngestionHelper ingestionHelper;

  @Override
  public void process(String innerJobId, LoadManifest loadManifest, IngestHeaders headers) {
    log.info("Start the internal async injection process. JobId: {}, loadManifest: {}, headers: {}",
        innerJobId, loadManifest, headers);

    jobStatusService.updateJobStatus(innerJobId, IngestJobStatus.RUNNING);
    InnerIngestResult ingestResult = InnerIngestResult.builder()
        .jobStatus(FAILED)
        .build();
    String summary;

    try {
      ingestResult = execute(innerJobId, loadManifest, headers);
      summary = COMPLETE.name();
    } catch (Exception e) {
      log.error("Inner ingestion job is failed. JobId: " + innerJobId, e);
      summary = ExceptionUtils.getRootCauseMessage(e);
    }

    IngestJob ingestJob = IngestJob.builder()
        .id(innerJobId)
        .status(ingestResult.getJobStatus())
        .summary(summary)
        .srns(ingestResult.getSrns())
        .build();
    jobStatusService.save(ingestJob);
    log.info("Finished the internal async injection process. Ingest job: {}", ingestJob);
  }

  private InnerIngestResult execute(String innerJobId, LoadManifest loadManifest,
      IngestHeaders headers) {
    String authorizationToken = headers.getAuthorizationToken();
    String partition = normalizePartition(headers.getPartition());
    String legalTags = headers.getLegalTags();

    IngestJobStatus[] ingestJobStatus = {COMPLETE};
    List<String> srns = new ArrayList<>();

    Map<String, String> groupEmailByName = delfiEntitlementsClient
        .getUserGroups(authorizationToken, portalProperties.getAppKey(), partition)
        .getGroups().stream()
        .collect(Collectors.toMap(Group::getName, Group::getEmail));
    log.info("Fetched the user groups aka permissions. JobId: {}, user groups: {}", innerJobId,
        groupEmailByName);

    ingestionHelper.getWorkProductComponents(loadManifest)
        .forEach(wpc -> {
          SchemaData schemaData = srnMappingService.getSchemaData(wpc.getResourceTypeId());

          RequestMeta requestMeta = RequestMeta.builder()
              .authorizationToken(authorizationToken)
              .partition(partition)
              .legalTags(legalTags)
              .schemaData(schemaData)
              .userGroupEmailByName(groupEmailByName)
              .resourceTypeId(new ResourceTypeId(wpc.getResourceTypeId()))
              .build();

          List<SubmittedFile> submittedFiles = wpc.getFiles().stream()
              .map(file -> delfiIngestionService.uploadFile(file, authorizationToken, partition))
              .map(file -> submitFile(file, requestMeta))
              .collect(Collectors.toList());

         // List<SubmittedFile> submittedFiles = new ArrayList<>();

          Map<String, SubmittedFile> jobIdToFile = submittedFiles.stream()
              .collect(Collectors.toMap(SubmittedFile::getJobId, Function.identity()));

          List<String> jobIds = new ArrayList<>(jobIdToFile.keySet());

          //poll job for readiness
          log.info("Waited for all submitted jobs to be finished. JobId: {}", innerJobId);
          JobsPullingResult jobsPullingResult = submitService.awaitSubmitJobs(jobIds, requestMeta);
          log.info("Jobs pulling result: {}", jobsPullingResult);

          // get record ID from ingestion job metadata
          List<IngestedFile> ingestedFiles = submitService.getIngestionResult(jobsPullingResult,
              jobIdToFile, requestMeta);
          log.info("Ingested files: {}", ingestedFiles);

          // fail records if at least one submit job fail
          log.info("Pulling ingestion job result: {}", jobsPullingResult);
          if (!jobsPullingResult.getFailedJobs().isEmpty()) {
            ingestJobStatus[0] = FAILED;
            delfiIngestionService.failSubmittedFiles(ingestedFiles, requestMeta);
            return;
          }

          //run enrichment
          List<EnrichedFile> enrichedFiles = ingestedFiles.stream()
              .map(file -> enrichService.enrichRecord(file, requestMeta, headers))
              .collect(Collectors.toList());
          log.info("Finished the enrichment. JobId: {}", innerJobId);

          // post validation
          enrichedFiles.stream()
              .map(file -> jsonValidationService
                  .validate(schemaData.getSchema(), getJsonNode(file.getRecord().getData())))
              .filter(result -> !result.isSuccess())
              .peek(result -> log.warn("Post submit record validation fail - " + result.toString()))
              .findFirst()
              .ifPresent(result -> {
                ingestJobStatus[0] = FAILED;
                List<Record> enrichedRecords = enrichedFiles.stream()
                    .map(EnrichedFile::getRecord)
                    .collect(Collectors.toList());
                delfiIngestionService.failRecords(enrichedRecords, requestMeta);
              });

          // save srn to record mapping
          List<String> fileSrns = enrichedFiles.stream().map(file -> {
            srnMappingService.saveSrnToRecord(SrnToRecord.builder()
                .recordId(file.getRecord().getId())
                .srn(file.getIngestedFile().getSubmittedFile().getSrn())
                .build());
            String fileSrn = file.getIngestedFile().getSubmittedFile().getSrn();
            srns.add(fileSrn);
            return fileSrn;
          }).collect(Collectors.toList());

          // create work product component record
          String wpcSrn = delfiIngestionService.createRecordForWorkProductComponent(wpc, fileSrns,
              requestMeta);
          srns.add(wpcSrn);
        });

    return InnerIngestResult.builder()
        .jobStatus(ingestJobStatus[0])
        .srns(srns)
        .build();
  }

  private SubmittedFile submitFile(SignedFile file, RequestMeta requestMeta) {
    SubmitJobResult result = submitService
        .submitFile(file.getRelativeFilePath(), requestMeta);

    return SubmittedFile.builder()
        .signedFile(file)
        .srn(result.getSrn())
        .jobId(result.getJobId())
        .build();
  }
}
