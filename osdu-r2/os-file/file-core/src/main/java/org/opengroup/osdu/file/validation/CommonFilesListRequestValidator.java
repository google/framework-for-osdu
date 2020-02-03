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

package org.opengroup.osdu.file.validation;

import java.time.LocalDateTime;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.file.FilesListRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonFilesListRequestValidator implements FilesListRequestValidator {

  private static final String TIME_FROM_FIELD = "TimeFrom";
  private static final String TIME_TO_FIELD = "TimeTo";
  private static final String PAGE_NUM_FIELD = "PageNum";
  private static final String ITEMS_FIELD = "Items";
  private static final String USER_ID_FIELD = "UserID";

  private static final String NOT_BLANK_MESSAGE = "{javax.validation.constraints.NotBlank.message}";
  private static final String NOT_NULL_MESSAGE = "{javax.validation.constraints.NotNull.message}";
  private static final String POSITIVE_OR_ZERO_MESSAGE =
      "{javax.validation.constraints.PositiveOrZero.message}";
  private static final String POSITIVE_MESSAGE = "{javax.validation.constraints.Positive.message}";

  @Override
  public boolean isValid(FilesListRequest request,
      ConstraintValidatorContext constraintValidatorContext) {
    boolean isValid = true;

    LocalDateTime from = request.getTimeFrom();
    LocalDateTime to = request.getTimeTo();

    if (from == null) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate(NOT_NULL_MESSAGE)
          .addPropertyNode(TIME_FROM_FIELD)
          .addConstraintViolation();
      isValid = false;
    }

    if (to == null) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate(NOT_NULL_MESSAGE)
          .addPropertyNode(TIME_TO_FIELD)
          .addConstraintViolation();
      isValid = false;
    }

    if (request.getPageNum() < 0) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate(POSITIVE_OR_ZERO_MESSAGE)
          .addPropertyNode(PAGE_NUM_FIELD)
          .addConstraintViolation();
      isValid = false;
    }

    if (request.getItems() <= 0) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate(POSITIVE_MESSAGE)
          .addPropertyNode(ITEMS_FIELD)
          .addConstraintViolation();
      isValid = false;
    }

    if (StringUtils.isBlank(request.getUserID())) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate(NOT_BLANK_MESSAGE)
          .addPropertyNode(USER_ID_FIELD)
          .addConstraintViolation();
      return false;
    }

    if (to != null && from != null && !from.isBefore(to)) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate("should be before TimeTo")
          .addPropertyNode(TIME_FROM_FIELD)
          .addConstraintViolation();
      isValid = false;
    }

    return isValid;
  }

}
