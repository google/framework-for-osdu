package com.osdu.function;

import com.osdu.model.job.IngestJobStatusDto;
import com.osdu.model.job.IngestJobStatusRequestPayload;
import com.osdu.service.JobStatusService;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobStatusFunction implements
    Function<Message<IngestJobStatusRequestPayload>, Message<IngestJobStatusDto>> {

  @Inject
  JobStatusService jobStatusService;

  @Override
  public Message<IngestJobStatusDto> apply(Message<IngestJobStatusRequestPayload> objectMessage) {
    log.info("Ingest job status request received, with following parameters: {}", objectMessage);
    final IngestJobStatusDto jobStatus = jobStatusService
        .getStatus(objectMessage.getPayload().getJobId(), objectMessage.getHeaders());
    log.info("Ingest job status result ready, request: {}, result:{}", objectMessage, jobStatus);
    return new GenericMessage<>(jobStatus);
  }
}
