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

import com.osdu.model.IngestProcessRequest;
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
public class IngestProcessFunction implements
    Function<Message<IngestProcessRequest>, Message<Boolean>> {

  final InnerIngestionProcess ingestionProcess;
  final JobStatusService jobStatusService;

  @Override
  public Message<Boolean> apply(Message<IngestProcessRequest> request) {
    log.debug("Ingest processing request received with following parameters: {}", request);

    IngestMessage ingestMessage = request.getPayload().getMessage().getIngestMessage();
    if (ingestMessage == null) {
      log.warn("Ingest message is null. Abort processing.");
      return new GenericMessage<>(false);
    }
    String ingestJobId = ingestMessage.getIngestJobId();

    IngestJob ingestJob = jobStatusService.get(ingestJobId);
    if (ingestJob.getStatus() != IngestJobStatus.CREATED) {
      log.warn("Ingestion job (jobId: {}) is already processing (status: {}). Ignore this message",
          ingestJobId, ingestJob.getStatus());
    } else {
      ingestionProcess.process(ingestJobId, ingestMessage.getLoadManifest(),
          ingestMessage.getHeaders());
      log.debug("Finish ingest processing. JobId: {}", ingestJobId);
    }

    log.debug("Ingest processing response ready. Request: {}, response: {}", request, null);
    return new GenericMessage<>(true);
  }
}
