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

import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;

public interface WorkflowStatusRepository {

  /**
   * Finds workflow status based on workflow id.
   *
   * @param workflowId workflow id
   * @return Workflow status
   */
  WorkflowStatus findWorkflowStatus(String workflowId);

  /**
   * Save workflow status.
   *
   * @param workflowStatus to save
   * @return saved workflow status
   */
  WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus);

  /**
   * Update workflow status based on workflow id.
   *
   * @param workflowId workflow id
   * @return Workflow status
   */
  WorkflowStatus updateWorkflowStatus(String workflowId, WorkflowStatusType workflowStatusType);
}
