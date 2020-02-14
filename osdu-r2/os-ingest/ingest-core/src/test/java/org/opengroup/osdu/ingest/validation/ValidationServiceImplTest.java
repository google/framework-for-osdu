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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import java.util.Collections;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.exception.OsduBadRequestException;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.opengroup.osdu.ingest.model.WorkProductLoadManifest;
import org.opengroup.osdu.ingest.property.DataTypeValidationProperties;
import org.opengroup.osdu.ingest.provider.interfaces.ValidationService;
import org.opengroup.osdu.ingest.validation.schema.LoadManifestValidationService;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class ValidationServiceImplTest {

  private static final String FILE_ID = "file-id";

  private static DataTypeValidationProperties dataTypeValidationProperties = mock(
      DataTypeValidationProperties.class);

  @Mock
  private LoadManifestValidationService loadManifestValidationService;

  private static Validator validator;
  private ValidationService validationService;

  @BeforeAll
  static void initAll() {
    HibernateValidatorConfiguration configuration = (HibernateValidatorConfiguration) Validation
        .byDefaultProvider()
        .configure();

    ValidatorFactory factory = configuration
        .constraintValidatorFactory(new TestConstraintValidatorFactory())
        .buildValidatorFactory();
    validator = factory.getValidator();
  }

  @BeforeEach
  void setUp() {
    validationService = new ValidationServiceImpl(validator, loadManifestValidationService);
    given(dataTypeValidationProperties.getAllowedDataTypes())
        .willReturn(Collections.singletonList(DataType.WELL_LOG));
  }

  @Nested
  class ValidateSubmitRequest {

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
    void shouldFailValidationIfWrongDataType() {
      // given
      SubmitRequest request = SubmitRequest.builder()
          .fileId(FILE_ID)
          .dataType(DataType.OSDU)
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

  @Nested
  class ValidateManifest {

    @Test
    void shouldSuccessfullyValidateManifest() {
      // given
      WorkProductLoadManifest loadManifest = WorkProductLoadManifest.builder()
          .build();
      given(loadManifestValidationService.validateManifest(loadManifest))
          .willReturn(Collections.emptySet());

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateManifest(loadManifest));

      // then
      then(thrown).isNull();
    }

    @Test
    void shouldThrownExceptionWhenValidationReturnsErrors() {
      // given
      WorkProductLoadManifest loadManifest = WorkProductLoadManifest.builder()
          .build();
      ValidationMessage message = ValidationMessage.of("type", ValidatorTypeCode.TYPE, "$.WorkProduct", "null", "object");
      given(loadManifestValidationService.validateManifest(loadManifest))
          .willReturn(Collections.singleton(message));

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateManifest(loadManifest));

      // then
      then(thrown)
          .isInstanceOf(OsduBadRequestException.class)
          .hasMessageMatching("Failed to validate json from manifest (.*), validation result is (.*)");
    }

  }

  static class TestConstraintValidatorFactory implements ConstraintValidatorFactory {

    ConstraintValidatorFactory constraintValidatorFactory = Validation
        .buildDefaultValidatorFactory().getConstraintValidatorFactory();

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
      if (SubmitRequestValidator.class.equals(key)) {
        return (T) new SubmitRequestValidator(dataTypeValidationProperties);
      }

      return constraintValidatorFactory.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }

}
