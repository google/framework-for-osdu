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

package org.opengroup.osdu.file.validation;

import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonLocationRequestValidator implements LocationRequestValidator {

  private static final String INVALID_FILE_ID = "Invalid FileID";
  private static final String FILE_ID_FIELD = "FileID";

  final FileIdValidator fileIdValidator;

  @Override
  public boolean isValid(LocationRequest request,
      ConstraintValidatorContext constraintValidatorContext) {
    String fileID = request.getFileID();

    if (fileID == null) {
      return true;
    }

    if (StringUtils.isBlank(fileID)) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate("{javax.validation.constraints.NotBlank.message}")
          .addPropertyNode(FILE_ID_FIELD)
          .addConstraintViolation();
      return false;
    }

    if (!fileIdValidator.checkFileID(fileID)) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate(INVALID_FILE_ID)
          .addPropertyNode(FILE_ID_FIELD)
          .addConstraintViolation();
      return false;
    }

    return true;
  }

}
