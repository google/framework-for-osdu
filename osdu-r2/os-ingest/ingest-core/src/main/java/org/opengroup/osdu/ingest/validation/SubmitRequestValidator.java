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

package org.opengroup.osdu.ingest.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmitRequestValidator
    implements ConstraintValidator<ValidSubmitRequest, SubmitRequest> {

  private static final String DATA_TYPE_FIELD = "DataType";
  private static final String FILE_ID_FIELD = "FileID";

  @Override
  public boolean isValid(SubmitRequest request,
      ConstraintValidatorContext constraintValidatorContext) {
    String fileID = request.getFileId();
    String dataType = request.getDataType();

    if (StringUtils.isBlank(fileID)) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate("{javax.validation.constraints.NotBlank.message}")
          .addPropertyNode(FILE_ID_FIELD)
          .addConstraintViolation();
      return false;
    }

    if (StringUtils.isBlank(dataType)) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate("{javax.validation.constraints.NotBlank.message}")
          .addPropertyNode(DATA_TYPE_FIELD)
          .addConstraintViolation();
      return false;
    }

    return true;
  }

}
