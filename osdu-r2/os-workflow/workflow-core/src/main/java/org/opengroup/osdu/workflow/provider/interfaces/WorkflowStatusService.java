/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.provider.interfaces;

import org.opengroup.osdu.core.common.exception.OsduUnauthorizedException;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.model.GetStatusResponse;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.UpdateStatusResponse;
import org.springframework.messaging.MessageHeaders;

public interface WorkflowStatusService {

  /**
   * GetWorkflowStatus returns status of workflow specified.
   *
   * @param request        getStatus request
   * @param messageHeaders message headers
   * @return workflow status.
   * @throws OsduUnauthorizedException if token and partitionID are missing or, invalid
   */
  GetStatusResponse getWorkflowStatus(GetStatusRequest request, MessageHeaders messageHeaders);

  /**
   * Update Workflow Status returns status of workflow specified.
   *
   * @param request        update status request
   * @param messageHeaders message headers
   * @return workflow status.
   * @throws OsduUnauthorizedException if token and partitionID are missing or, invalid
   */
  UpdateStatusResponse updateWorkflowStatus(UpdateStatusRequest request,
      MessageHeaders messageHeaders);

}
