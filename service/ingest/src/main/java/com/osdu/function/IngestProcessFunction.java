package com.osdu.function;

import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.job.IngestMessage;
import com.osdu.service.JobStatusService;
import com.osdu.service.processing.InnerIngestionProcess;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class IngestProcessFunction implements Function<Message<IngestMessage>, Message<Boolean>> {

  final InnerIngestionProcess ingestionProcess;
  final JobStatusService jobStatusService;

  @Override
  public Message<Boolean> apply(Message<IngestMessage> request) {
    log.info("Ingest processing request received with following parameters: {}", request);

    IngestMessage ingestMessage = request.getPayload();
    String ingestJobId = ingestMessage.getIngestJobId();

    if (ingestJobId == null) {
      log.error("Ingest job id is null. Abort processing.");
      return new GenericMessage<>(false);
    }

    IngestJob ingestJob = jobStatusService.get(ingestJobId);
    if (ingestJob.getStatus() != IngestJobStatus.CREATED) {
      log.info("Ingestion job (jobId: {}) is already processing (status: {}). Ignore this message",
          ingestJobId, ingestJob.getStatus());
    } else {
      ingestionProcess.process(ingestJobId, ingestMessage.getLoadManifest(),
          ingestMessage.getHeaders());
      log.info("Finish ingest processing. JobId: {}", ingestJobId);
    }

    log.info("Ingest processing response ready. Request: {}, response: {}", request, null);
    return new GenericMessage<>(true);
  }
}
