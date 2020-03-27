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

package org.opengroup.osdu.workflow.provider.gcp.mapper;

import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.springframework.stereotype.Component;

@Component
public class EnumMapper {

  /**
   * Map the {@link WorkflowType} to a lower case string.
   *
   * @param workflowType workflowType
   * @return lower case string
   */
  public String asString(WorkflowType workflowType) {
    if (workflowType == null) {
      return null;
    }
    return workflowType.name().toLowerCase();
  }

  /**
   * Map a lower case string to the {@link WorkflowType}
   *
   * @param workflowType workflowType
   * @return {@link WorkflowType} constant
   */
  public WorkflowType toWorkflowType(String workflowType) {
    if (workflowType == null) {
      return null;
    }
    return WorkflowType.valueOf(workflowType.toUpperCase());
  }

  /**
   * Map the {@link WorkflowStatusType} to a lower case string.
   *
   * @param workflowStatusType workflowStatusType
   * @return lower case string
   */
  public String asString(WorkflowStatusType workflowStatusType) {
    if (workflowStatusType == null) {
      return null;
    }
    return workflowStatusType.name().toLowerCase();
  }

  /**
   * Map a lower case string to the {@link WorkflowStatusType}
   *
   * @param workflowStatusType workflowStatusType
   * @return {@link WorkflowStatusType} constant
   */
  public WorkflowStatusType toWorkflowStatusType(String workflowStatusType) {
    if (workflowStatusType == null) {
      return null;
    }
    return WorkflowStatusType.valueOf(workflowStatusType.toUpperCase());
  }

}
