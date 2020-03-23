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

package org.opengroup.osdu.workflow.validation;

import java.util.Map;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonStartWorkflowRequestValidator implements IStartWorkflowRequestValidator {

  private static final String WORKFLOW_TYPE_FIELD = "WorkflowType";
  private static final String CONTEXT_FIELD = "Context";

  @Override
  public boolean isValid(StartWorkflowRequest request, ConstraintValidatorContext context) {

    WorkflowType workflowType = request.getWorkflowType();

    if (workflowType == null) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("{javax.validation.constraints.NotNull.message}")
          .addPropertyNode(WORKFLOW_TYPE_FIELD)
          .addConstraintViolation();
      return false;
    }

    Map<String, Object> payload = request.getContext();

    if (payload == null) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("{javax.validation.constraints.NotNull.message}")
          .addPropertyNode(CONTEXT_FIELD)
          .addConstraintViolation();
      return false;
    }

    return true;
  }

}
