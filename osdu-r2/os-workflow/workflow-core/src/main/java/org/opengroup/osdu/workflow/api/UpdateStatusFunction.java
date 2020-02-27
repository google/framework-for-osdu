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

package org.opengroup.osdu.workflow.api;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.UpdateStatusResponse;
import org.opengroup.osdu.workflow.provider.interfaces.WorkflowStatusService;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateStatusFunction
    implements Function<Message<UpdateStatusRequest>, Message<UpdateStatusResponse>> {

  final WorkflowStatusService workflowStatusService;

  @Override
  public Message<UpdateStatusResponse> apply(Message<UpdateStatusRequest> message) {
    UpdateStatusResponse response = workflowStatusService.updateWorkflowStatus(message.getPayload(),
        message.getHeaders());
    return new GenericMessage<>(response);
  }
}
