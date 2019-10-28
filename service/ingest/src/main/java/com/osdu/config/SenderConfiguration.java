package com.osdu.config;

import com.osdu.model.job.IngestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Configuration
@Slf4j
public class SenderConfiguration {

  private static final String TOPIC_NAME = "osdu.service.ingest";

  @Bean
  public DirectChannel pubSubOutputChannel() {
    return new DirectChannel();
  }

  @Bean
  @ServiceActivator(inputChannel = "pubSubOutputChannel")
  public MessageHandler messageSender(PubSubTemplate pubSubTemplate) {
    PubSubMessageHandler adapter = new PubSubMessageHandler(pubSubTemplate, TOPIC_NAME);
    adapter.setPublishCallback(new ListenableFutureCallback<String>() {
      @Override
      public void onFailure(Throwable ex) {
        log.info("There was an error sending the message.", ex);
      }

      @Override
      public void onSuccess(String result) {
        log.info("Message was sent successfully. {}", result);
      }
    });

    return adapter;
  }

  /**
   * an interface that allows sending a person to Pub/Sub.
   */
  @MessagingGateway(defaultRequestChannel = "pubSubOutputChannel")
  public interface PubSubIngestGateway {
    void sendIngestToPubSub(IngestMessage ingestMessage);
  }

}
