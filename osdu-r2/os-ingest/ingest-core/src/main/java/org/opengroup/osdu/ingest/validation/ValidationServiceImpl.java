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

package org.opengroup.osdu.ingest.validation;

import com.networknt.schema.ValidationMessage;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.opengroup.osdu.core.common.exception.OsduBadRequestException;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.opengroup.osdu.ingest.model.WorkProductLoadManifest;
import org.opengroup.osdu.ingest.validation.schema.LoadManifestValidationService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

  final Validator validator;
  final LoadManifestValidationService loadManifestValidationService;

  @Override
  public void validateSubmitRequest(SubmitRequest request) {
    Set<ConstraintViolation<SubmitRequest>> constraintViolations =
        validator.validate(request, ValidationSequence.class);
    if (CollectionUtils.isNotEmpty(constraintViolations)) {
      throw new ConstraintViolationException("Invalid Submit request", constraintViolations);
    }
  }

  @Override
  public void validateManifest(WorkProductLoadManifest loadManifest) {
    Set<ValidationMessage> errors = loadManifestValidationService.validateManifest(loadManifest);
    if (CollectionUtils.isNotEmpty(errors)) {
      throw new OsduBadRequestException(String.format(
          "Failed to validate json from manifest %s, validation result is %s",
          loadManifest, errors));
    }
  }

}
