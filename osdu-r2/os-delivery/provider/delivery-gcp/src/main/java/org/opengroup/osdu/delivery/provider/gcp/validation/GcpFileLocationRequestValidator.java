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

package org.opengroup.osdu.delivery.provider.gcp.validation;

import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.delivery.provider.gcp.model.constant.StorageConstant;
import org.opengroup.osdu.delivery.validation.CommonFileLocationRequestValidator;
import org.opengroup.osdu.delivery.validation.FileIdValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class GcpFileLocationRequestValidator extends CommonFileLocationRequestValidator {

  private static final String FILE_ID_MAX_GCP_LENGTH =
      "length should be less than resulted GCS filepath. Max: ${max_length}";

  public GcpFileLocationRequestValidator(FileIdValidator fileIdValidator) {
    super(fileIdValidator);
  }

  @Override
  public boolean isValid(FileLocationRequest request,
      ConstraintValidatorContext context) {
    boolean isValid = super.isValid(request, context);

    if (!isValid) {
      return isValid;
    }

    HibernateConstraintValidatorContext hibernateContext =
        context.unwrap(HibernateConstraintValidatorContext.class);

    if (request.getFileID().length() > StorageConstant.GCS_MAX_FILEPATH) {
      hibernateContext.disableDefaultConstraintViolation();
      hibernateContext
          .addExpressionVariable("max_length", StorageConstant.GCS_MAX_FILEPATH)
          .buildConstraintViolationWithTemplate(FILE_ID_MAX_GCP_LENGTH)
          .addPropertyNode(FILE_ID_FIELD)
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
