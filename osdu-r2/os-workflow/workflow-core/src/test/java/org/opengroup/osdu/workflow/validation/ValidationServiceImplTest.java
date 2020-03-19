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

package org.opengroup.osdu.workflow.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import java.util.HashMap;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.config.RequestConstraintMappingContributor;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.ValidationService;

@DisplayNameGeneration(ReplaceCamelCase.class)
class ValidationServiceImplTest {

  private static final String WORKFLOW_ID = "WorkflowID";
  private static final String STATUS = "Status";
  private static final String NOT_BLANK_MESSAGE = "must not be blank";
  private static final String NOT_ALLOWED_MESSAGE = "Not allowed workflow status type: SUBMITTED, Should be one of: [RUNNING, FINISHED, FAILED]";
  private static final String NOT_NULL_MESSAGE = "must not be null";
  private static final String WORKFLOW_ID_VALUE = "workflow-id";
  private static final String WORKFLOW_TYPE = "WorkflowType";
  private static final String CONTEXT = "Context";
  private static final String DATA_TYPE = "test-type";

  private static Validator validator;
  private ValidationService validationService;

  @BeforeAll
  static void initAll() {
    HibernateValidatorConfiguration configuration = (HibernateValidatorConfiguration) Validation
        .byDefaultProvider()
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
  class ValidGetStatusRequest {

    @Test
    void shouldSuccessfullyValidateProperRequest() {
      // given
      GetStatusRequest request = GetStatusRequest.builder().workflowId(WORKFLOW_ID_VALUE).build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateGetStatusRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @Test
    void shouldFailValidationWhenRequestHasBlankWorkflowId() {
      // given
      GetStatusRequest request = GetStatusRequest.builder()
          .workflowId(" ").build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateGetStatusRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid GetStatus request");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(WORKFLOW_ID, NOT_BLANK_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenThereIsNoWorkflowId() {
      // given
      GetStatusRequest request = GetStatusRequest.builder()
          .workflowId(null).build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateGetStatusRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid GetStatus request");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(WORKFLOW_ID, NOT_BLANK_MESSAGE));
    }
  }

  @Nested
  class ValidUpdateStatusRequest {

    @Test
    void shouldSuccessfullyValidateProperRequest() {
      // given
      UpdateStatusRequest request = UpdateStatusRequest.builder().workflowId(WORKFLOW_ID_VALUE)
          .workflowStatusType(WorkflowStatusType.RUNNING).build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateUpdateStatusRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @Test
    void shouldFailValidationWhenRequestHasBlankWorkflowId() {
      // given
      UpdateStatusRequest request = UpdateStatusRequest.builder()
          .workflowId(" ")
          .workflowStatusType(WorkflowStatusType.RUNNING).build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateUpdateStatusRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid Update Workflow Status request");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(WORKFLOW_ID, NOT_BLANK_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenThereIsNoWorkflowId() {
      // given
      UpdateStatusRequest request = UpdateStatusRequest.builder()
          .workflowId(null)
          .workflowStatusType(WorkflowStatusType.RUNNING).build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateUpdateStatusRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid Update Workflow Status request");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(WORKFLOW_ID, NOT_BLANK_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenThereIsNoWorkflowStatusType() {
      // given
      UpdateStatusRequest request = UpdateStatusRequest.builder()
          .workflowId(WORKFLOW_ID_VALUE)
          .workflowStatusType(null).build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateUpdateStatusRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid Update Workflow Status request");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(STATUS, NOT_NULL_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenWorkflowStatusTypeIsNotAllowed() {
      // given
      UpdateStatusRequest request = UpdateStatusRequest.builder()
          .workflowId(WORKFLOW_ID_VALUE)
          .workflowStatusType(WorkflowStatusType.SUBMITTED).build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateUpdateStatusRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid Update Workflow Status request");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(STATUS, NOT_ALLOWED_MESSAGE));
    }
  }

  @Nested
  class ValidateStartWorkflowRequest {

    @Test
    void shouldSuccessfullyValidateWhenRequestHasValidValues() {
      // given
      StartWorkflowRequest request = StartWorkflowRequest.builder()
          .workflowType(WorkflowType.INGEST)
          .dataType(DATA_TYPE)
          .context(new HashMap<>()).build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateStartWorkflowRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @Test
    void shouldFailValidationWhenThereIsNoWorkflowType() {
      // given
      StartWorkflowRequest request = StartWorkflowRequest.builder()
          .workflowType(null)
          .dataType(DATA_TYPE)
          .context(new HashMap<>()).build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateStartWorkflowRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid StartWorkflowRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(WORKFLOW_TYPE, NOT_NULL_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenThereIsNoContext() {
      // given
      StartWorkflowRequest request = StartWorkflowRequest.builder()
          .workflowType(WorkflowType.INGEST)
          .dataType(DATA_TYPE)
          .context(null).build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateStartWorkflowRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid StartWorkflowRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(CONTEXT, NOT_NULL_MESSAGE));
    }
  }

  static class TestConstraintValidatorFactory implements ConstraintValidatorFactory {

    ConstraintValidatorFactory constraintValidatorFactory = Validation
        .buildDefaultValidatorFactory().getConstraintValidatorFactory();

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
      if (StartWorkflowRequestValidatorWrapper.class.equals(key)) {
        return (T) new StartWorkflowRequestValidatorWrapper(
            new CommonStartWorkflowRequestValidator());
      }

      if (GetStatusRequestValidatorWrapper.class.equals(key)) {
        return (T) new GetStatusRequestValidatorWrapper(new CommonGetStatusRequestValidator());
      }

      if (UpdateStatusRequestValidatorWrapper.class.equals(key)) {
        return (T) new UpdateStatusRequestValidatorWrapper(
            new CommonUpdateStatusRequestValidator());
      }

      return constraintValidatorFactory.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }

}
