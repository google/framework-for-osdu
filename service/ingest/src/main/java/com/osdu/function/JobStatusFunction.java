package com.osdu.function;

import com.osdu.model.job.IngestJobStatusDto;
import com.osdu.model.job.IngestJobStatusRequestPayload;
import com.osdu.service.JobStatusService;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobStatusFunction implements
    Function<Message<IngestJobStatusRequestPayload>, Message<IngestJobStatusDto>> {

  final JobStatusService jobStatusService;

  @Override
  public Message<IngestJobStatusDto> apply(Message<IngestJobStatusRequestPayload> objectMessage) {
    log.debug("Ingest job status request received, with following parameters: {}", objectMessage);
    final IngestJobStatusDto jobStatus = jobStatusService
        .getStatus(objectMessage.getPayload().getJobId(), objectMessage.getHeaders());
    log.debug("Ingest job status result ready, request: {}, result:{}", objectMessage, jobStatus);
    return new GenericMessage<>(jobStatus);
  }
}
