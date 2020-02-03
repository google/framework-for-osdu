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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.ingest.model.SubmitRequest;

@DisplayNameGeneration(ReplaceCamelCase.class)
class ValidationServiceTest {

  private static final String FILE_ID = "file-id";

  private static Validator validator;
  private ValidationService validationService;

  @BeforeAll
  static void initAll() {
    ValidatorFactory factory = Validation.byDefaultProvider()
        .configure()
        .buildValidatorFactory();
    validator = factory.getValidator();
  }

  @BeforeEach
  void setUp() {
    validationService = new ValidationService(validator);
  }


  @Test
  void shouldSuccessfullyValidateEmptyRequest() {
    // given
    SubmitRequest request = SubmitRequest.builder()
        .fileId(FILE_ID)
        .dataType(DataType.WELL_LOG)
        .build();

    // when
    Throwable thrown = catchThrowable(() -> validationService.validateSubmitRequest(request));

    // then
    assertThat(thrown).isNull();
  }

  @Test
  void shouldFailValidationIfNoDataType() {
    // given
    SubmitRequest request = SubmitRequest.builder()
        .fileId(FILE_ID)
        .build();

    // when
    Throwable thrown = catchThrowable(() -> validationService.validateSubmitRequest(request));

    // then
    assertThat(thrown)
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessage("Invalid Submit request");
  }

  @Test
  void shouldFailValidationIfNoFileId() {
    // given
    SubmitRequest request = SubmitRequest.builder()
        .dataType(DataType.WELL_LOG)
        .build();

    // when
    Throwable thrown = catchThrowable(() -> validationService.validateSubmitRequest(request));

    // then
    assertThat(thrown)
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessage("Invalid Submit request");
  }

}
