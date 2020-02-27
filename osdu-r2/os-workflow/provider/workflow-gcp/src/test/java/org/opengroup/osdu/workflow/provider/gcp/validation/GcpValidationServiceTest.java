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

package org.opengroup.osdu.workflow.provider.gcp.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;
import static org.opengroup.osdu.workflow.provider.gcp.validation.GcpUpdateStatusRequestValidator.DATASTORE_MAX_VALUE_SIZE;

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
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.config.RequestConstraintMappingContributor;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.ValidationService;
import org.opengroup.osdu.workflow.validation.CommonUpdateStatusRequestValidator;
import org.opengroup.osdu.workflow.validation.UpdateStatusRequestValidatorWrapper;
import org.opengroup.osdu.workflow.validation.ValidationServiceImpl;

@DisplayNameGeneration(ReplaceCamelCase.class)
class GcpValidationServiceTest {

  private static final String WORKFLOW_ID_FIELD = "WorkflowID";
  private static final String NOT_BLANK_MESSAGE = "must not be blank";
  private static final String WORKFLOW_ID = "workflow-id";

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
  class ValidateUpdateStatusRequest {

    @Test
    void shouldSuccessfullyValidate() {
      // given
      UpdateStatusRequest request = UpdateStatusRequest.builder()
          .workflowId(WORKFLOW_ID)
          .workflowStatusType(WorkflowStatusType.RUNNING)
          .build();

      // when
      Throwable thrown = catchThrowable(
          () -> validationService.validateUpdateStatusRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @Test
    void shouldNotExecuteGcpSpecificValidationWhenCommonValidationIsFailed() {
      // given
      UpdateStatusRequest request = UpdateStatusRequest.builder()
          .workflowId("")
          .workflowStatusType(WorkflowStatusType.RUNNING)
          .build();

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
          .containsExactly(tuple(WORKFLOW_ID_FIELD, NOT_BLANK_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenWorkflowIdIsTooLong() {
      // given
      UpdateStatusRequest request = UpdateStatusRequest.builder()
          .workflowId(RandomStringUtils.randomAlphanumeric(DATASTORE_MAX_VALUE_SIZE + 1))
          .workflowStatusType(WorkflowStatusType.RUNNING)
          .build();

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
          .containsExactly(tuple(WORKFLOW_ID_FIELD, "value length should be less than 1500"));
    }

  }

  static class TestConstraintValidatorFactory implements ConstraintValidatorFactory {

    ConstraintValidatorFactory constraintValidatorFactory = Validation
        .buildDefaultValidatorFactory().getConstraintValidatorFactory();

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {

      if (UpdateStatusRequestValidatorWrapper.class.equals(key)) {
        CommonUpdateStatusRequestValidator updateStatusRequestValidator =
            new GcpUpdateStatusRequestValidator();
        return (T) new UpdateStatusRequestValidatorWrapper(updateStatusRequestValidator);
      }

      return constraintValidatorFactory.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }

}
