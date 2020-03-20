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

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.provider.interfaces.IValidationService;
import org.opengroup.osdu.workflow.validation.group.IValidationSequence;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationServiceImpl implements IValidationService {

  final Validator validator;

  @Override
  public void validateGetStatusRequest(GetStatusRequest request) {
    validate(request, "Invalid GetStatus request");
  }

  @Override
  public void validateUpdateStatusRequest(UpdateStatusRequest request) {
    validate(request, "Invalid Update Workflow Status request");
  }

  @Override
  public void validateStartWorkflowRequest(StartWorkflowRequest request) {
    validate(request, "Invalid StartWorkflowRequest");
  }

  private <T> void validate(T value, String errorMsg) {
    Set<ConstraintViolation<T>> constraintViolations =
        validator.validate(value, IValidationSequence.class);
    if (CollectionUtils.isNotEmpty(constraintViolations)) {
      throw new ConstraintViolationException(errorMsg, constraintViolations);
    }
  }
}
