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

package com.osdu.service.delfi;

import static com.osdu.model.delfi.status.MasterJobStatus.RUNNING;
import static com.osdu.service.JsonUtils.toJson;
import static com.osdu.service.JsonUtils.toObject;

import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.model.RequestContext;
import com.osdu.model.ResourceType;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.delfi.Acl;
import com.osdu.model.delfi.DelfiFile;
import com.osdu.model.delfi.DelfiIngestedFile;
import com.osdu.model.delfi.SaveRecordsResult;
import com.osdu.model.delfi.SuccessMetadata;
import com.osdu.model.delfi.status.JobPollingResult;
import com.osdu.model.delfi.status.JobStatusResponse;
import com.osdu.model.delfi.status.MasterJobStatus;
import com.osdu.model.delfi.submit.AclObject;
import com.osdu.model.delfi.submit.FileInput;
import com.osdu.model.delfi.submit.SubmitFileContext;
import com.osdu.model.delfi.submit.SubmitFileObject;
import com.osdu.model.delfi.submit.SubmitFileResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.delfi.submit.ingestor.IngestorRoutine;
import com.osdu.model.delfi.submit.ingestor.LasIngestor;
import com.osdu.model.delfi.submit.ingestor.LasIngestorRoutine;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.model.property.SubmitProperties;
import com.osdu.service.SubmitService;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiSubmitService implements SubmitService {

  private static final String GCS_PROTOCOL = "gs:/";
  private static final String SUCCESS_METADATA_JSON_PATH = "successrecords/success-metadata.json";

  final DelfiPortalProperties portalProperties;
  final SubmitProperties submitProperties;
  final RestTemplate restTemplate;
  final DelfiIngestionClient delfiIngestionClient;
  final DelfiPortalService portalService;

  @Override
  public JobPollingResult awaitSubmitJob(String jobId, RequestContext requestContext) {
    JobStatusResponse submittedJob;
    MasterJobStatus currentStatus;

    long attempts = 0;
    long pollingInterval = submitProperties.getPollingInterval();
    long cycles = submitProperties.getPollingCycles();

    log.debug("Awaiting submitted job. JobId: {}, polling interval: {}, cycles: {}",
        jobId, pollingInterval, cycles);

    do {
      attempts++;
      submittedJob = delfiIngestionClient.getJobStatus(jobId,
          requestContext.getAuthorizationToken(),
          portalProperties.getAppKey(),
          requestContext.getPartition());
      currentStatus = submittedJob.getJobInfo().getMasterJobStatus();

      if (currentStatus == RUNNING && attempts < cycles) {
        try {
          Thread.sleep(pollingInterval);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IngestException("Pulling submitted job was unexpected interrupted. JobId = "
              + jobId, e);
        }
      }
    } while (currentStatus == RUNNING && attempts < cycles);

    log.debug("Finished pooling job status. JobId: {}, status: {} attempts: {}",
        jobId, currentStatus, attempts);

    return JobPollingResult.builder()
        .runningJob(jobId)
        .job(submittedJob)
        .status(currentStatus)
        .build();
  }

  @Override
  public SubmitJobResult submitFile(SubmitFileContext fileContext, RequestContext requestContext) {
    SubmitFileResult submitFileResult = delfiIngestionClient
        .submitFile(requestContext.getAuthorizationToken(),
            portalProperties.getAppKey(),
            requestContext.getPartition(),
            requestContext.getPartition(),
            SubmitFileObject.builder()
                .kind(fileContext.getKind())
                .acl(getAcl(requestContext.getUserGroupEmailByName()))
                .legalTags(requestContext.getLegalTags())
                .filePath(GCS_PROTOCOL + fileContext.getRelativeFilePath())
                .fileInput(FileInput.FILE_PATH)
                .ingestorRoutines(getIngestorRoutines(fileContext))
                .build());

    return SubmitJobResult.builder()
        .jobId(submitFileResult.getJobId())
        .build();
  }

  @Override
  public DelfiIngestedFile getIngestedFile(SubmittedFile file, JobStatusResponse response,
      RequestContext requestContext) {
    String location = GCS_PROTOCOL + response.getSummary().getOutputLocation()
        + SUCCESS_METADATA_JSON_PATH;
    log.debug("File location of ingestion job success metadata: {}", location);
    DelfiFile delfiFile = portalService.getFile(location, requestContext.getAuthorizationToken(),
        requestContext.getPartition());
    String fileContent = restTemplate.getForObject(delfiFile.getSignedUrl(), String.class);
    log.debug("Success metadata: {}", fileContent);
    SuccessMetadata metadata = toObject(fileContent, SuccessMetadata.class);
    SaveRecordsResult saveResult = toObject(metadata.getMessage(), SaveRecordsResult.class);
    log.debug("Save records result: {}", saveResult);

    return DelfiIngestedFile.builder()
        .submittedFile(file)
        .recordId(saveResult.getRecordIds().get(0))
        .build();
  }

  private String getAcl(Map<String, String> groupEmailByName) {
    return toJson(AclObject.builder()
        .acl(Acl.builder()
            .owner(groupEmailByName.get("data.default.owners"))
            .viewer(groupEmailByName.get("data.default.viewers"))
            .build())
        .build());
  }

  private String getIngestorRoutines(SubmitFileContext fileContext) {
    IngestorRoutine ingestorRoutine = null;
    ResourceType wpcType = new ResourceTypeId(fileContext.getWpcResourceTypeId())
        .getResourceType();
    ResourceType fileType = new ResourceTypeId(fileContext.getFileResourceTypeId())
        .getResourceType();

    if (wpcType == ResourceType.WPC_WELL_LOG && fileType == ResourceType.FILE_LAS2) {
      ingestorRoutine = LasIngestorRoutine.builder()
          .lasIngestor(LasIngestor.builder()
              .createRawWellRecord(true)
              .build())
          .build();
    }

    return ingestorRoutine == null ? null : toJson(Collections.singletonList(ingestorRoutine));
  }

}
