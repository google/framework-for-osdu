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

package org.opengroup.osdu.file.gcp.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.config.RequestConstraintMappingContributor;
import org.opengroup.osdu.file.provider.interfaces.ValidationService;
import org.opengroup.osdu.file.validation.CommonFileLocationRequestValidator;
import org.opengroup.osdu.file.validation.FileIdValidator;
import org.opengroup.osdu.file.validation.FileLocationRequestValidatorWrapper;
import org.opengroup.osdu.file.validation.ValidationServiceImpl;

@DisplayNameGeneration(ReplaceCamelCase.class)
class GcpValidationServiceTest {

  private static final String FILE_ID_FIELD = "FileID";

  private static final String NOT_BLANK_MESSAGE = "must not be blank";

  private static final String FILE_ID = "temp-file.tmp";

  private static Validator validator;
  private ValidationService validationService;

  @BeforeAll
  static void initAll() {
    HibernateValidatorConfiguration configuration = (HibernateValidatorConfiguration) Validation.byDefaultProvider()
        .configure();

    RequestConstraintMappingContributor requestConstraintMappingContributor
        = new RequestConstraintMappingContributor();
    requestConstraintMappingContributor.createConstraintMappings(() -> {
      DefaultConstraintMapping mapping = new DefaultConstraintMapping();
      configuration.addMapping(mapping);
      return mapping;
    });

    ValidatorFactory factory = configuration
        .constraintValidatorFactory(new TestConstraintValidatorFactory())
        .buildValidatorFactory();
    validator = factory.getValidator();
  }

  @BeforeEach
  void setUp() {
    validationService = new ValidationServiceImpl(validator);
  }

  @Nested
  class ValidateFileLocationRequest {

    @Test
    void shouldSuccessfullyValidateWhenRequestHasValidFileId() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(FILE_ID)
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateFileLocationRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @Test
    void shouldNotExecuteGcpSpecificValidationWhenCommonValidationIsFailed() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(" ")
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateFileLocationRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid FileLocationRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(FILE_ID_FIELD, NOT_BLANK_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenRequestHasToLargeFileId() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(RandomStringUtils.randomAlphanumeric(1050))
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateFileLocationRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid FileLocationRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(FILE_ID_FIELD, "length should be less than resulted GCS filepath. Max: 1024"));
    }

  }

  static class TestConstraintValidatorFactory implements ConstraintValidatorFactory {

    ConstraintValidatorFactory constraintValidatorFactory = Validation
        .buildDefaultValidatorFactory().getConstraintValidatorFactory();

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {

      if (FileLocationRequestValidatorWrapper.class.equals(key)) {
        CommonFileLocationRequestValidator fileLocationRequestValidator =
            new GcpFileLocationRequestValidator(new FileIdValidator());
        return (T) new FileLocationRequestValidatorWrapper(fileLocationRequestValidator);
      }

      return constraintValidatorFactory.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }

}
