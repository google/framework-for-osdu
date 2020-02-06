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

package org.opengroup.osdu.ingest.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.opengroup.osdu.ingest.TestUtils.getFeignRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.ingest.client.WorkflowServiceClient;
import org.opengroup.osdu.ingest.exception.OsduServerErrorException;
import org.opengroup.osdu.ingest.model.Headers;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class WorkflowIntegrationServiceTest {

  private static final String WORKFLOW_SERVICE_URL = "http://workflowServiceUrl";
  private static final String TEST_AUTH_TOKEN = "test-auth-token";
  private static final String TEST_PARTITION = "test-partition";
  private static final String WORKFLOW_ID = "workflow-id";
  private ObjectMapper mapper = new ObjectMapper();

  @Mock
  private WorkflowServiceClient workflowServiceClient;

  @Captor
  ArgumentCaptor<StartWorkflowRequest> workflowRequestCaptor;

  WorkflowIntegrationService workflowIntegrationService;

  @BeforeEach
  void setUp() {
    workflowIntegrationService = new WorkflowIntegrationService(workflowServiceClient, mapper);
  }

  @Test
  void shouldSubmitIngestToWorkflowService() throws JsonProcessingException {

    // given
    Headers requestHeaders = Headers.builder()
        .authorizationToken(TEST_AUTH_TOKEN)
        .partitionID(TEST_PARTITION)
        .build();

    StartWorkflowResponse startWorkflowResponse = StartWorkflowResponse.builder().workflowId(WORKFLOW_ID).build();

    Response response = Response.builder()
        .body(mapper.writeValueAsString(startWorkflowResponse), StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(HttpStatus.OK.value()).build();

    Map<String, Object> context = new HashMap<>();
    context.put("key", "value");
    given(workflowServiceClient.startWorkflow(eq(TEST_AUTH_TOKEN), eq(TEST_PARTITION), any()))
        .willReturn(response);

    // when
    String workflowId = workflowIntegrationService
        .submitIngestToWorkflowService(DataType.WELL_LOG, context, requestHeaders);

    // then
    then(workflowId).isEqualTo(WORKFLOW_ID);
    verify(workflowServiceClient)
        .startWorkflow(anyString(), anyString(), workflowRequestCaptor.capture());
    then(workflowRequestCaptor.getValue()).satisfies(request -> {
      then(request.getContext()).containsEntry("key", "value");
      then(request.getDataType()).isEqualTo(DataType.WELL_LOG);
      then(request.getWorkflowType()).isEqualTo(WorkflowType.INGEST);
    });
  }

  @Test
  void shouldThrowExceptionIfResponseIsEmpty() throws JsonProcessingException {

    // given
    Headers requestHeaders = Headers.builder()
        .authorizationToken(TEST_AUTH_TOKEN)
        .partitionID(TEST_PARTITION)
        .build();

    StartWorkflowResponse startWorkflowResponse = StartWorkflowResponse.builder().workflowId(null).build();

    Response response = Response.builder()
        .body(mapper.writeValueAsString(startWorkflowResponse), StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(HttpStatus.OK.value()).build();

    Map<String, Object> context = new HashMap<>();
    context.put("key", "value");
    given(workflowServiceClient.startWorkflow(eq(TEST_AUTH_TOKEN), eq(TEST_PARTITION), any()))
        .willReturn(response);

    // when
    Throwable thrown = catchThrowable(() ->  workflowIntegrationService
        .submitIngestToWorkflowService(DataType.WELL_LOG, context, requestHeaders));

    // then
    then(thrown).isInstanceOf(OsduServerErrorException.class);
    then(thrown.getMessage()).isEqualTo("No workflow id in workflow service response");
  }
}
