package com.osdu.config;

import com.osdu.model.job.IngestJob;
import java.util.ArrayList;
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
import org.springframework.messaging.handler.annotation.Header;

@Configuration
@Slf4j
public class ReceiverConfiguration {

  private static final String SUBSCRIPTION_NAME = "osdu.service.ingest.sub";

  private final ArrayList<IngestJob> processedIngestJobList = new ArrayList<>();

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
    adapter.setPayloadType(IngestJob.class);
    return adapter;
  }

  @ServiceActivator(inputChannel = "pubSubInputChannel")
  public void messageReceiver(IngestJob ingestJob,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
    log.info("Message arrived! Payload: " + ingestJob);
    this.processedIngestJobList.add(ingestJob);
    log.info("Sleeping.. " + ingestJob.getId());
    try {
      for (int i = 1; i <= 100; i++) {
        int t = 10 * i;
        log.info("Iteration: " + t);
        Thread.sleep(t);
        log.info("Woke Up! " + t);
      }
    } catch (InterruptedException e) {
      log.error("Fail", e);
    }
    log.info("Finish! " + ingestJob.getId());
    message.ack();
  }

  @Bean
  @Qualifier("ProcessedPersonsList")
  public ArrayList<IngestJob> processedIngestJobList() {
    return this.processedIngestJobList;
  }

}
