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

package com.osdu.service.google;

import static com.osdu.request.OsduHeader.extractHeaderByName;

import com.google.common.collect.ImmutableMap;
import com.osdu.exception.IngestJobNotFoundException;
import com.osdu.mapper.IngestJobMapper;
import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.job.IngestJobStatusDto;
import com.osdu.repository.IngestJobRepository;
import com.osdu.request.OsduHeader;
import com.osdu.service.AuthenticationService;
import com.osdu.service.JobStatusService;
import java.util.UUID;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GcpJobStatusService implements JobStatusService {

  final AuthenticationService authenticationService;
  final IngestJobRepository ingestJobRepository;
  @Named
  final IngestJobMapper ingestJobMapper;

  @Override
  public IngestJobStatusDto getStatus(String jobId, MessageHeaders headers) {
    log.debug("Request for getting a injection job status. JobId: {}, headers: {}", jobId, headers);

    String authorizationToken = extractHeaderByName(headers, OsduHeader.AUTHORIZATION);
    String partition = extractHeaderByName(headers, OsduHeader.PARTITION);

    authenticationService
        .checkAuthentication(authorizationToken, partition);

    IngestJobStatusDto jobStatusDto = ingestJobRepository.findById(jobId)
        .map(ingestJobMapper::toStatusDto)
        .orElseThrow(() -> new IngestJobNotFoundException("Not ingest job found by id = " + jobId));

    log.debug("Found the injection job status: {}", jobStatusDto);
    return jobStatusDto;
  }

  @Override
  public IngestJob get(String jobId) {
    log.debug("Request for getting a injection job. JobId: {}", jobId);
    return ingestJobRepository.findById(jobId)
        .orElseThrow(() -> new IngestJobNotFoundException("Not ingest job found by id = " + jobId));
  }

  @Override
  public String initInjectJob() {
    log.debug("Initiating a new injection job");
    String jobId = UUID.randomUUID().toString();
    ingestJobRepository.save(IngestJob.builder()
        .id(jobId)
        .status(IngestJobStatus.CREATED)
        .build());

    log.debug("Created a new running injection job. JobId: {}", jobId);
    return jobId;
  }

  @Override
  public void updateJobStatus(String jobId, IngestJobStatus status) {
    log.debug("Update the injection job status. JobId: {}, status: {}", jobId, status);
    ingestJobRepository.updateFields(jobId, ImmutableMap.of("status", status.name()));
    log.debug("Updated the injection job status. Status: {}", status);
  }

  @Override
  public void save(IngestJob ingestJob) {
    log.debug("Update the ingestion job. Job: {}", ingestJob);
    ingestJobRepository.save(ingestJob);
  }
}
