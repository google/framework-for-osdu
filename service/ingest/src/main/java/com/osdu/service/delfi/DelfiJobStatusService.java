package com.osdu.service.delfi;

import com.osdu.model.dto.SchemaDataDto;
import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.job.IngestJobStatusDto;
import com.osdu.repository.IngestJobRepository;
import com.osdu.repository.google.GcpSchemaDataRepository;
import com.osdu.service.JobStatusService;
import javax.inject.Inject;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
public class DelfiJobStatusService implements JobStatusService {

  @Inject
  IngestJobRepository ingestJobRepository;

  @Override
  public IngestJobStatusDto getStatus(String jobId, MessageHeaders headers) {

    IngestJob job = ingestJobRepository.findById(jobId);
    return IngestJobStatusDto.fromIngestJob(job);
  }
}
