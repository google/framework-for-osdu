/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.delivery.validation;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.delivery.provider.interfaces.ValidationService;
import org.opengroup.osdu.delivery.validation.group.ValidationSequence;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

  final Validator validator;

  /**
   * Validates location request using Java Bean Validation.
   *
   * @param request location request
   * @throws ConstraintViolationException if request is invalid
   */
  @Override
  public void validateLocationRequest(LocationRequest request) {
    validate(request, "Invalid LocationRequest");
  }

  /**
   * Validates file location request using Java Bean Validation.
   *
   * @param request location request
   * @throws ConstraintViolationException if request is invalid
   */
  @Override
  public void validateFileLocationRequest(FileLocationRequest request) {
    validate(request, "Invalid FileLocationRequest");
  }

  /**
   * Validates file list request using Java Bean Validation.
   *
   * @param request location request
   * @throws ConstraintViolationException if request is invalid
   */
  @Override
  public void validateFileListRequest(FileListRequest request) {
    validate(request, "Invalid FileListRequest");
  }

  private <T> void validate(T value, String errorMsg) {
    Set<ConstraintViolation<T>> constraintViolations =
        validator.validate(value, ValidationSequence.class);
    if (CollectionUtils.isNotEmpty(constraintViolations)) {
      throw new ConstraintViolationException(errorMsg, constraintViolations);
    }
  }

}
