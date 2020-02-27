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

package org.opengroup.osdu.workflow.provider.gcp.validation;

import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.validation.CommonUpdateStatusRequestValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class GcpUpdateStatusRequestValidator extends CommonUpdateStatusRequestValidator {

  static final Integer DATASTORE_MAX_VALUE_SIZE = 1500;
  private static final String WORKFLOW_ID_FIELD = "WorkflowID";
  private static final String FILE_ID_MAX_GCP_LENGTH =
      "value length should be less than ${max_length}";

  @Override
  public boolean isValid(UpdateStatusRequest request,
      ConstraintValidatorContext context) {
    boolean isValid = super.isValid(request, context);

    if (!isValid) {
      return isValid;
    }

    HibernateConstraintValidatorContext hibernateContext =
        context.unwrap(HibernateConstraintValidatorContext.class);

    if (request.getWorkflowId().getBytes().length >= DATASTORE_MAX_VALUE_SIZE) {
      hibernateContext.disableDefaultConstraintViolation();
      hibernateContext
          .addExpressionVariable("max_length", DATASTORE_MAX_VALUE_SIZE)
          .buildConstraintViolationWithTemplate(FILE_ID_MAX_GCP_LENGTH)
          .addPropertyNode(WORKFLOW_ID_FIELD)
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
