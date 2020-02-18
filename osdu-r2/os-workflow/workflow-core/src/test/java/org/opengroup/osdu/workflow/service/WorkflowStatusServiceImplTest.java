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

package org.opengroup.osdu.workflow.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.Headers;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.mapper.HeadersMapper;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.model.GetStatusResponse;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.UpdateStatusResponse;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.AuthenticationService;
import org.opengroup.osdu.workflow.provider.interfaces.ValidationService;
import org.opengroup.osdu.workflow.provider.interfaces.WorkflowStatusRepository;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class WorkflowStatusServiceImplTest {

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String WORKFLOW_ID = "workflow-id";

  @Spy
  private HeadersMapper headersMapper = Mappers.getMapper(HeadersMapper.class);
  @Mock
  private AuthenticationService authenticationService;
  @Mock
  private ValidationService validationService;
  @Mock
  private WorkflowStatusRepository workflowStatusRepository;

  WorkflowStatusServiceImpl workflowStatusService;

  @BeforeEach
  void setUp() {
    workflowStatusService = new WorkflowStatusServiceImpl(headersMapper, authenticationService,
        validationService, workflowStatusRepository);
  }

  @Test
  void shouldGetWorkflowStatus() {

    // given
    MessageHeaders headers = getMessageHeaders();
    GetStatusRequest request = GetStatusRequest.builder().workflowId(WORKFLOW_ID).build();

    WorkflowStatus workflowStatus = WorkflowStatus.builder()
        .workflowStatusType(WorkflowStatusType.SUBMITTED)
        .workflowId(WORKFLOW_ID)
        .submittedAt(new Date()).build();

    given(workflowStatusRepository.findWorkflowStatus(eq(WORKFLOW_ID))).willReturn(workflowStatus);

    // when
    GetStatusResponse workflowStatusResponse = workflowStatusService
        .getWorkflowStatus(request, headers);

    // then
    then(workflowStatusResponse.getWorkflowStatusType()).isEqualTo(WorkflowStatusType.SUBMITTED);
    InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
        workflowStatusRepository);
    inOrder.verify(headersMapper).toHeaders(headers);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(validationService).validateGetStatusRequest(request);
    inOrder.verify(workflowStatusRepository)
        .findWorkflowStatus(eq(WORKFLOW_ID));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowExceptionIfThereIsNoWorkflow() {

    // given
    MessageHeaders headers = getMessageHeaders();
    GetStatusRequest request = GetStatusRequest.builder().workflowId(WORKFLOW_ID).build();

    given(workflowStatusRepository.findWorkflowStatus(eq(WORKFLOW_ID))).willReturn(null);

    // when
    Throwable thrown = catchThrowable(
        () -> workflowStatusService.getWorkflowStatus(request, headers));

    // then
    then(thrown).isInstanceOf(WorkflowNotFoundException.class);
  }

  @Test
  void shouldUpdateWorkflowStatus() {

    // given
    MessageHeaders headers = getMessageHeaders();

    UpdateStatusRequest request = UpdateStatusRequest.builder()
        .workflowId(WORKFLOW_ID)
        .workflowStatusType(WorkflowStatusType.RUNNING).build();

    WorkflowStatus workflowStatus = WorkflowStatus.builder()
        .workflowStatusType(WorkflowStatusType.RUNNING)
        .workflowId(WORKFLOW_ID)
        .submittedAt(new Date()).build();

    given(workflowStatusRepository
        .updateWorkflowStatus(eq(WORKFLOW_ID), eq(WorkflowStatusType.RUNNING)))
        .willReturn(workflowStatus);

    // when
    UpdateStatusResponse updateStatusResponse = workflowStatusService
        .updateWorkflowStatus(request, headers);

    // then
    then(updateStatusResponse.getWorkflowStatusType()).isEqualTo(WorkflowStatusType.RUNNING);
    InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
        workflowStatusRepository);
    inOrder.verify(headersMapper).toHeaders(headers);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(validationService).validateUpdateStatusRequest(request);
    inOrder.verify(workflowStatusRepository)
        .updateWorkflowStatus(eq(WORKFLOW_ID), eq(WorkflowStatusType.RUNNING));
    inOrder.verifyNoMoreInteractions();
  }

  private MessageHeaders getMessageHeaders() {
    Map<String, Object> headers = new HashMap<>();
    headers.put(Headers.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.put(Headers.PARTITION, PARTITION);

    return new MessageHeaders(headers);
  }
}
