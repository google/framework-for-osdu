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

package org.opengroup.osdu.workflow.validation;

import static org.opengroup.osdu.workflow.model.WorkflowStatusType.FAILED;
import static org.opengroup.osdu.workflow.model.WorkflowStatusType.FINISHED;
import static org.opengroup.osdu.workflow.model.WorkflowStatusType.RUNNING;

import java.util.Arrays;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateStatusValidator
    implements ConstraintValidator<ValidUpdateStatusRequest, UpdateStatusRequest> {

  private static final String WORKFLOW_ID_FIELD = "FileID";
  private static final String WORKFLOW_STATUS_FIELD = "Status";
  private static final List<WorkflowStatusType> VALID_STATUSES = Arrays
      .asList(RUNNING, FINISHED, FAILED);

  @Override
  public boolean isValid(UpdateStatusRequest request, ConstraintValidatorContext context) {

    String workflowId = request.getWorkflowId();
    WorkflowStatusType workflowStatusType = request.getWorkflowStatusType();

    if (StringUtils.isBlank(workflowId)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("{javax.validation.constraints.NotBlank.message}")
          .addPropertyNode(WORKFLOW_ID_FIELD)
          .addConstraintViolation();
      return false;
    }

    if (workflowStatusType == null) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("{javax.validation.constraints.NotNull.message}")
          .addPropertyNode(WORKFLOW_STATUS_FIELD)
          .addConstraintViolation();
      return false;
    }

    if (!VALID_STATUSES.contains(workflowStatusType)) {
      String msg = String
          .format("Not allowed workflow status type: %s, Should be one of: %s", workflowStatusType,
              VALID_STATUSES);
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(msg)
          .addPropertyNode(WORKFLOW_STATUS_FIELD)
          .addConstraintViolation();
      return false;
    }

    return true;
  }

}
