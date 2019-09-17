package com.osdu.function;

import com.osdu.model.IngestResult;
import com.osdu.model.LoadManifest;
import com.osdu.service.IngestService;
import java.util.function.Function;
import javax.inject.Inject;
import org.springframework.messaging.Message;

public class IngestFunction implements Function<Message<LoadManifest>, Message<IngestResult>> {

  @Inject
  IngestService ingestService;

  @Override
  public Message<IngestResult> apply(Message<LoadManifest> objectMessage) {
    ingestService.ingestManifest(objectMessage.getPayload(), objectMessage.getHeaders());
    return null;
  }
}
