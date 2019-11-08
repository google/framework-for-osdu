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

import com.osdu.model.job.IngestJobStatusDto;
import com.osdu.model.job.IngestJobStatusRequestPayload;
import com.osdu.service.JobStatusService;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobStatusFunction implements
    Function<Message<IngestJobStatusRequestPayload>, Message<IngestJobStatusDto>> {

  final JobStatusService jobStatusService;

  @Override
  public Message<IngestJobStatusDto> apply(Message<IngestJobStatusRequestPayload> objectMessage) {
    log.debug("Ingest job status request received, with following parameters: {}", objectMessage);
    final IngestJobStatusDto jobStatus = jobStatusService
        .getStatus(objectMessage.getPayload().getJobId(), objectMessage.getHeaders());
    log.debug("Ingest job status result ready, request: {}, result:{}", objectMessage, jobStatus);
    return new GenericMessage<>(jobStatus);
  }
}
