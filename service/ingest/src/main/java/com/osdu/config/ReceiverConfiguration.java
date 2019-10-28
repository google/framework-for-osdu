package com.osdu.config;

import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.job.IngestMessage;
import com.osdu.service.JobStatusService;
import com.osdu.service.processing.InnerIngestionProcess;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;

@Configuration
@Slf4j
public class ReceiverConfiguration {

  private static final String SUBSCRIPTION_NAME = "osdu.service.ingest.sub";

  @Inject
  InnerIngestionProcess ingestionProcess;
  @Inject
  JobStatusService jobStatusService;

  @Bean
  public DirectChannel pubSubInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter messageChannelAdapter(
      @Qualifier("pubSubInputChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, SUBSCRIPTION_NAME);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    adapter.setPayloadType(IngestMessage.class);
    return adapter;
  }

  @ServiceActivator(inputChannel = "pubSubInputChannel")
  public void messageReceiver(IngestMessage ingestMessage,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
    log.info("Message arrived! Payload: " + ingestMessage);
    String ingestJobId = ingestMessage.getIngestJobId();
    IngestJob ingestJob = jobStatusService.get(ingestJobId);
    if (ingestJob.getStatus() != IngestJobStatus.CREATED) {
      log.info("Ingestion job (jobId: {}) is already processing. Ignore this message", ingestJobId);
    } else {
      ingestionProcess.process(ingestJobId, ingestMessage.getLoadManifest(),
          new MessageHeaders((ingestMessage.getHeaders())));
      log.info("Finish! " + ingestJobId);
    }

    message.ack();
  }

}
