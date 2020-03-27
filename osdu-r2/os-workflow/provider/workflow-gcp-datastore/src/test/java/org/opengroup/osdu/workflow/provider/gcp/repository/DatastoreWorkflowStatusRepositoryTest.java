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

package org.opengroup.osdu.workflow.provider.gcp.repository;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.SUBMITTED_AT;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusNotFoundException;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusNotUpdatedException;
import org.opengroup.osdu.workflow.provider.gcp.mapper.EnumMapper;
import org.opengroup.osdu.workflow.provider.gcp.mapper.IWorkflowStatusMapper;
import org.opengroup.osdu.workflow.provider.gcp.model.WorkflowStatusEntity;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class DatastoreWorkflowStatusRepositoryTest {

  private static final String TEST_WORKFLOW_ID = "test-workflow-id";
  private static final String TEST_AIRFLOW_RUN_ID = "test-airflow-run-id";
  private static final String USER = "user-1";

  @Spy
  private IWorkflowStatusMapper workflowStatusMapper = Mappers.getMapper(IWorkflowStatusMapper.class);
  @Mock
  private IWorkflowStatusEntityRepository workflowStatusEntityRepository;

  private IWorkflowStatusRepository workflowStatusRepository;

  @BeforeEach
  void setUp() {
    EnumMapper enumMapper = new EnumMapper();
    ReflectionTestUtils.setField(workflowStatusMapper, "enumMapper", enumMapper);

    workflowStatusRepository = new DatastoreWorkflowStatusRepository(workflowStatusMapper,
        workflowStatusEntityRepository);
  }

  @Nested
  class FindWorkflowStatus {

    @Test
    void shouldFindWorkflowStatusByWorkflowId() {

      // given
      Date createdDate = new Date();

      given(workflowStatusEntityRepository.findByWorkflowId(TEST_WORKFLOW_ID))
          .willReturn(getWorkflowStatusEntity(createdDate));

      // when
      WorkflowStatus workflowStatus = workflowStatusRepository
          .findWorkflowStatus(TEST_WORKFLOW_ID);

      // then
      then(workflowStatus).isEqualTo(getWorkflowStatus(createdDate));
    }

  }

  @Nested
  class SaveWorkflowStatus {

    @Captor
    private ArgumentCaptor<WorkflowStatusEntity> entityCaptor;

    @Test
    void shouldSaveWorkflowStatusAndReturnSavedEntity() {
      // given
      Date createdDate = new Date();
      WorkflowStatus workflowStatus = getWorkflowStatus(createdDate);

      given(workflowStatusEntityRepository.save(any(WorkflowStatusEntity.class)))
          .willAnswer(DatastoreWorkflowStatusRepositoryTest.this::copyWorkflowStatusEntity);

      // when
      WorkflowStatus saved = workflowStatusRepository.saveWorkflowStatus(workflowStatus);

      // then
      then(saved).isEqualTo(workflowStatus);

      InOrder inOrder = Mockito.inOrder(workflowStatusEntityRepository, workflowStatusMapper);
      inOrder.verify(workflowStatusEntityRepository).save(any(WorkflowStatusEntity.class));
      inOrder.verify(workflowStatusMapper).toWorkflowStatus(any(WorkflowStatusEntity.class));
      inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldUseServerTimestampWhenCreateAtIsNotSpecified() {
      // given
      WorkflowStatus workflowStatus = WorkflowStatus.builder()
          .workflowId(TEST_WORKFLOW_ID)
          .airflowRunId(TEST_AIRFLOW_RUN_ID)
          .workflowStatusType(WorkflowStatusType.SUBMITTED)
          .submittedBy(USER)
          .build();

      given(workflowStatusEntityRepository.save(any(WorkflowStatusEntity.class)))
          .willAnswer(DatastoreWorkflowStatusRepositoryTest.this::copyWorkflowStatusEntity);

      // when
      WorkflowStatus saved = workflowStatusRepository.saveWorkflowStatus(workflowStatus);

      // then
      then(saved).isEqualToIgnoringGivenFields(saved, SUBMITTED_AT);
      then(saved.getSubmittedAt()).isBefore(new Date());

      InOrder inOrder = Mockito.inOrder(workflowStatusEntityRepository, workflowStatusMapper);
      inOrder.verify(workflowStatusEntityRepository).save(any(WorkflowStatusEntity.class));
      inOrder.verify(workflowStatusMapper).toWorkflowStatus(any(WorkflowStatusEntity.class));
      inOrder.verifyNoMoreInteractions();

    }

  }

  @Nested
  class UpdateWorkflowStatus {

    @Test
    void shouldUpdateWorkflowStatusAndReturnSavedEntity() {
      // given
      Date createdDate = new Date();

      given(workflowStatusEntityRepository.findByWorkflowId(TEST_WORKFLOW_ID))
          .willReturn(getWorkflowStatusEntity(createdDate));
      given(workflowStatusEntityRepository.save(any(WorkflowStatusEntity.class)))
          .willAnswer(DatastoreWorkflowStatusRepositoryTest.this::copyWorkflowStatusEntity);

      // when
      WorkflowStatus saved = workflowStatusRepository
          .updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.RUNNING);

      // then
      then(saved.getWorkflowStatusType()).isEqualTo(WorkflowStatusType.RUNNING);
      then(saved.getWorkflowId()).isEqualTo(TEST_WORKFLOW_ID);

      InOrder inOrder = Mockito.inOrder(workflowStatusEntityRepository, workflowStatusMapper);
      inOrder.verify(workflowStatusEntityRepository).findByWorkflowId(TEST_WORKFLOW_ID);
      inOrder.verify(workflowStatusEntityRepository).save(any(WorkflowStatusEntity.class));
      inOrder.verify(workflowStatusMapper).toWorkflowStatus(any(WorkflowStatusEntity.class));
      inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowExceptionWhenNothingWasFound() {
      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository
          .updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.RUNNING));

      // then
      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusNotFoundException.class)
          .hasMessage("Workflow status for Workflow id: test-workflow-id not found");

      InOrder inOrder = Mockito.inOrder(workflowStatusEntityRepository, workflowStatusMapper);
      inOrder.verify(workflowStatusEntityRepository).findByWorkflowId(TEST_WORKFLOW_ID);
      inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowExceptionIfWorkflowHasAlreadyDefinedStatus() {
      // given
      Date createdDate = new Date();
      given(workflowStatusEntityRepository.findByWorkflowId(TEST_WORKFLOW_ID))
          .willReturn(getWorkflowStatusEntity(createdDate));

      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository.updateWorkflowStatus(
          TEST_WORKFLOW_ID, WorkflowStatusType.SUBMITTED));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusNotUpdatedException.class)
          .hasMessage(
              "Workflow status for workflow id: test-workflow-id already has status:SUBMITTED and can not be updated");

      InOrder inOrder = Mockito.inOrder(workflowStatusEntityRepository, workflowStatusMapper);
      inOrder.verify(workflowStatusEntityRepository).findByWorkflowId(TEST_WORKFLOW_ID);
      inOrder.verifyNoMoreInteractions();
    }

  }

  private WorkflowStatusEntity getWorkflowStatusEntity(Date createdDate) {
    return WorkflowStatusEntity.builder()
        .workflowId(TEST_WORKFLOW_ID)
        .airflowRunId(TEST_AIRFLOW_RUN_ID)
        .workflowStatusType(WorkflowStatusType.SUBMITTED.name())
        .submittedAt(createdDate)
        .submittedBy(USER)
        .build();
  }

  private WorkflowStatus getWorkflowStatus(Date createdDate) {
    return WorkflowStatus.builder()
        .workflowId(TEST_WORKFLOW_ID)
        .airflowRunId(TEST_AIRFLOW_RUN_ID)
        .workflowStatusType(WorkflowStatusType.SUBMITTED)
        .submittedAt(createdDate)
        .submittedBy(USER)
        .build();
  }

  private WorkflowStatusEntity copyWorkflowStatusEntity(InvocationOnMock invocation) {
    WorkflowStatusEntity saved = invocation.getArgument(0);
    WorkflowStatusEntity copied = new WorkflowStatusEntity();
    BeanUtils.copyProperties(saved, copied);
    return copied;
  }

}
