package com.osdu.service.google;

import static com.osdu.request.OsduHeader.extractHeaderByName;

import com.google.common.collect.ImmutableMap;
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
    log.info("Request for getting a injection job status. JobId: {}, headers: {}", jobId, headers);

    String authorizationToken = extractHeaderByName(headers, OsduHeader.AUTHORIZATION);
    String partition = extractHeaderByName(headers, OsduHeader.PARTITION);

    authenticationService
        .checkAuthentication(authorizationToken, partition);

    IngestJob job = ingestJobRepository.findById(jobId);

    log.info("Found the injection job: {}", job);
    return ingestJobMapper.toStatusDto(job);
  }

  @Override
  public IngestJob get(String jobId) {
    log.info("Request for getting a injection job. JobId: {}", jobId);
    return ingestJobRepository.findById(jobId);
  }

  @Override
  public String initInjectJob() {
    log.info("Initiating a new injection job");
    String jobId = UUID.randomUUID().toString();
    ingestJobRepository.save(IngestJob.builder()
        .id(jobId)
        .status(IngestJobStatus.CREATED)
        .build());

    log.info("Created a new running injection job. JobId: {}", jobId);
    return jobId;
  }

  @Override
  public void updateJobStatus(String jobId, IngestJobStatus status) {
    log.info("Update the injection job status. JobId: {}, status: {}", jobId, status);
    ingestJobRepository.updateFields(jobId, ImmutableMap.of("status", status.name()));
    log.info("Updated the injection job status. Status: {}", status);
  }

  @Override
  public void save(IngestJob ingestJob) {
    log.info("Update the ingestion job. Job: {}", ingestJob);
    ingestJobRepository.save(ingestJob);
  }
}
