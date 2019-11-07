package com.osdu.function;

import com.osdu.model.IngestResult;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.service.InitialIngestService;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class IngestFunction implements Function<Message<LoadManifest>, Message<IngestResult>> {

  final InitialIngestService ingestService;

  @Override
  public Message<IngestResult> apply(Message<LoadManifest> objectMessage) {
    log.debug("Ingest request received, with following parameters: {}", objectMessage);
    final IngestResult ingestResult = ingestService
        .ingestManifest(objectMessage.getPayload(), objectMessage.getHeaders());
    log.debug("Ingest result ready, request: {}, result:{}", objectMessage, ingestResult);
    return new GenericMessage<>(ingestResult);
  }
}
