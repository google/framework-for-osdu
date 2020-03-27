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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.assertj.core.groups.Tuple;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.delivery.ReplaceCamelCase;
import org.opengroup.osdu.delivery.config.RequestConstraintMappingContributor;
import org.opengroup.osdu.delivery.provider.interfaces.ValidationService;

@DisplayNameGeneration(ReplaceCamelCase.class)
class ValidationServiceImplTest {

  private static final String FILE_ID_FIELD = "FileID";
  private static final String TIME_FROM_FIELD = "TimeFrom";
  private static final String TIME_TO_FIELD = "TimeTo";
  private static final String PAGE_NUM_FIELD = "PageNum";
  private static final String ITEMS_FIELD = "Items";
  private static final String USER_ID_FIELD = "UserID";

  private static final String NOT_BLANK_MESSAGE = "must not be blank";
  private static final String NOT_NULL_MESSAGE = "must not be null";
  private static final String POSITIVE_OR_ZERO_MESSAGE = "must be greater than or equal to 0";
  private static final String POSITIVE_MESSAGE = "must be greater than 0";

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
  class ValidLocationRequest {

    @Test
    void shouldSuccessfullyValidateEmptyRequest() {
      // given
      LocationRequest request = LocationRequest.builder()
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateLocationRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @Test
    void shouldSuccessfullyValidateRequestWithFileId() {
      // given
      LocationRequest request = LocationRequest.builder()
          .fileID(FILE_ID)
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateLocationRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @Test
    void shouldFailValidationWhenRequestHasBlankFileId() {
      // given
      LocationRequest request = LocationRequest.builder()
          .fileID("")
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateLocationRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid LocationRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(FILE_ID_FIELD, NOT_BLANK_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenRequestHasInvalidFileId() {
      // given
      LocationRequest request = LocationRequest.builder()
          .fileID("..ddd.com")
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateLocationRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid LocationRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(FILE_ID_FIELD, "Invalid FileID"));
    }

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
    void shouldFailValidationWhenRequestHasBlankFileId() {
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
    void shouldFailValidationWhenRequestHasNullFileId() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
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
    void shouldFailValidationWhenRequestHasInvalidFileId() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID("temp-file.")
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
          .containsExactly(tuple(FILE_ID_FIELD, "Invalid FileID"));
    }

  }

  @Nested
  class ValidateFileListRequest {

    @Test
    void shouldSuccessfullyValidateFilledRequest() {
      // given
      LocalDateTime now = LocalDateTime.now();
      FileListRequest request = FileListRequest.builder()
          .timeFrom(now.minusHours(3))
          .timeTo(now)
          .pageNum(0)
          .items((short) 1)
          .userID("temp-user")
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateFileListRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @ParameterizedTest(name = "{index} ==> Validation should fail when {0}")
    @MethodSource("org.opengroup.osdu.file.validation.ValidationServiceTest#fileListRequestProvider")
    void shouldFailValidationWhenRequest(ArgumentsAccessor arguments) {
      // given
      FileListRequest request = arguments.get(1, FileListRequest.class);

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateFileListRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid FileListRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      List<Tuple> expectedTuples = arguments.get(2, List.class);
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsOnlyElementsOf(expectedTuples);
    }

  }

  static Stream<Arguments> fileListRequestProvider() {
    LocalDateTime now = LocalDateTime.now();
    FileListRequest request1 = FileListRequest.builder()
        .timeTo(now)
        .pageNum(0)
        .items((short) 1)
        .userID("temp-user")
        .build();
    FileListRequest request2 = FileListRequest.builder()
        .timeFrom(now.minusHours(3))
        .pageNum(0)
        .items((short) 1)
        .userID("temp-user")
        .build();
    FileListRequest request3 = FileListRequest.builder()
        .timeFrom(now.minusHours(3))
        .timeTo(now)
        .pageNum(-2)
        .items((short) 1)
        .userID("temp-user")
        .build();
    FileListRequest request4 = FileListRequest.builder()
        .timeFrom(now.minusHours(3))
        .timeTo(now)
        .pageNum(0)
        .items((short) -1)
        .userID("temp-user")
        .build();
    FileListRequest request5 = FileListRequest.builder()
        .timeFrom(now.minusHours(3))
        .timeTo(now)
        .pageNum(0)
        .items((short) 1)
        .build();
    FileListRequest request6 = FileListRequest.builder()
        .timeFrom(now)
        .timeTo(now.minusHours(3))
        .pageNum(0)
        .items((short) 1)
        .userID("temp-user")
        .build();
    FileListRequest request7 = FileListRequest.builder()
        .build();

    return Stream.of(
        arguments("request has null TimeFrom", request1, singletonList(tuple(TIME_FROM_FIELD, NOT_NULL_MESSAGE))),
        arguments("request has null TimeTo", request2, singletonList(tuple(TIME_TO_FIELD, NOT_NULL_MESSAGE))),
        arguments("request has negative PageNum", request3, singletonList(tuple(PAGE_NUM_FIELD, POSITIVE_OR_ZERO_MESSAGE))),
        arguments("request has negative Items ", request4, singletonList(tuple(ITEMS_FIELD, POSITIVE_MESSAGE))),
        arguments("request has null UserId", request5, singletonList(tuple(USER_ID_FIELD, NOT_BLANK_MESSAGE))),
        arguments("request has TimeFrom after TimeTo", request6, singletonList(tuple(TIME_FROM_FIELD, "should be before TimeTo"))),
        arguments("request is empty", request7, Arrays.asList(
            tuple(TIME_FROM_FIELD, NOT_NULL_MESSAGE),
            tuple(TIME_TO_FIELD, NOT_NULL_MESSAGE),
            tuple(ITEMS_FIELD, POSITIVE_MESSAGE),
            tuple(USER_ID_FIELD, NOT_BLANK_MESSAGE)
        ))
    );
  }

  static class TestConstraintValidatorFactory implements ConstraintValidatorFactory {

    ConstraintValidatorFactory constraintValidatorFactory = Validation
        .buildDefaultValidatorFactory().getConstraintValidatorFactory();

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
      if (LocationRequestValidatorWrapper.class.equals(key)) {
        LocationRequestValidator locationRequestValidator = new CommonLocationRequestValidator(
            new FileIdValidator());
        return (T) new LocationRequestValidatorWrapper(locationRequestValidator);
      }

      if (FileLocationRequestValidatorWrapper.class.equals(key)) {
        CommonFileLocationRequestValidator fileLocationRequestValidator =
            new CommonFileLocationRequestValidator(new FileIdValidator());
        return (T) new FileLocationRequestValidatorWrapper(fileLocationRequestValidator);
      }

      if (FileListRequestValidatorWrapper.class.equals(key)) {
        return (T) new FileListRequestValidatorWrapper(new CommonFileListRequestValidator());
      }

      return constraintValidatorFactory.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }

}
