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

package com.osdu.function;

import com.osdu.model.IngestResult;
import com.osdu.model.type.manifest.LoadManifest;
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
