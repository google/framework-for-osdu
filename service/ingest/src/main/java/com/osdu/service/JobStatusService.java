package com.osdu.service;

import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.job.IngestJobStatusDto;
import org.springframework.messaging.MessageHeaders;

public interface JobStatusService {

  IngestJobStatusDto getStatus(String jobId, MessageHeaders headers);

  IngestJob get(String jobId);

  String initInjectJob();

  void updateJobStatus(String jobId, IngestJobStatus status);

  void save(IngestJob ingestJob);

}
