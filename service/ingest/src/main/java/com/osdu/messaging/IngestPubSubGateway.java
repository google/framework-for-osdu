package com.osdu.messaging;

import com.osdu.model.job.IngestMessage;
import org.springframework.integration.annotation.MessagingGateway;

/**
 * an interface that allows sending a ingest message to Pub/Sub.
 */
@MessagingGateway(defaultRequestChannel = "pubSubOutputChannel")
public interface IngestPubSubGateway {

  void sendIngestToPubSub(IngestMessage ingestMessage);

}
