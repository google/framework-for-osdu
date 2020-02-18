/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.functions;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.model.GetStatusResponse;
import org.opengroup.osdu.workflow.provider.interfaces.WorkflowStatusService;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetStatusFunction
    implements Function<Message<GetStatusRequest>, Message<GetStatusResponse>> {

  final WorkflowStatusService workflowStatusService;

  @Override
  public Message<GetStatusResponse> apply(Message<GetStatusRequest> message) {
    GetStatusResponse response = workflowStatusService.getWorkflowStatus(message.getPayload(),
        message.getHeaders());
    return new GenericMessage<>(response);
  }
}
