package com.osdu.function;

import com.osdu.model.IngestResult;
import com.osdu.model.LoadManifest;
import com.osdu.service.IngestService;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
public class IngestFunction implements Function<Message<LoadManifest>, Message<IngestResult>> {

  @Inject
  IngestService ingestService;

  @Override
  public Message<IngestResult> apply(Message<LoadManifest> objectMessage) {
    log.info("Ingest request received, with following parameters: {}", objectMessage);
    final IngestResult ingestResult = ingestService
        .ingestManifest(objectMessage.getPayload(), objectMessage.getHeaders());
    log.info("Ingest result ready, request: {}, result:{}", objectMessage, ingestResult);
    return new GenericMessage<>(ingestResult);
  }
}
