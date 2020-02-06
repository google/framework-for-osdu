/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Configuration
@Slf4j
public class IngestSenderConfiguration {

  private static final String TOPIC_NAME = "osdu.service.ingest";

  @Bean
  public DirectChannel pubSubOutputChannel() {
    return new DirectChannel();
  }

  /**
   * Sends a message to the queue that will trigger the worker to start the ingestion.
   * @param pubSubTemplate template to use
   * @return handler for the message once it was sent
   */
  @Bean
  @ServiceActivator(inputChannel = "pubSubOutputChannel")
  public MessageHandler ingestMessageSender(PubSubTemplate pubSubTemplate) {
    PubSubMessageHandler adapter = new PubSubMessageHandler(pubSubTemplate, TOPIC_NAME);
    adapter.setPublishCallback(new ListenableFutureCallback<String>() {
      @Override
      public void onFailure(Throwable ex) {
        log.error("There was an error sending the message.", ex);
      }

      @Override
      public void onSuccess(String result) {
        log.debug("Message was sent successfully. {}", result);
      }
    });

    return adapter;
  }

}
