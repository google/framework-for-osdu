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

package org.opengroup.osdu.ingest;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.opengroup.osdu.ingest.TestUtils.getFeignRequest;
import static org.opengroup.osdu.ingest.model.Headers.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.Test;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.ingest.client.FileServiceClient;
import org.opengroup.osdu.ingest.client.WorkflowServiceClient;
import org.opengroup.osdu.ingest.model.Headers;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.opengroup.osdu.ingest.model.SubmitResponse;
import org.opengroup.osdu.ingest.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(ReplaceCamelCase.class)
public class IngestFlowTest {

  private static final String WORKFLOW_ID = "workflow-id";
  private static final String TEST_FILE_LOCATION = "http://test-file-location";
  private static final String TEST_AUTH = "test-auth";
  private static final String PARTITION = "partition";
  private static final String FILE_ID = "file-id";
  private static final String GCP = "GCP";

  @Autowired
  private MockMvc mockMvc;
  private ObjectMapper mapper = new ObjectMapper();

  @MockBean
  private AuthenticationService authenticationService;
  @MockBean
  private FileServiceClient fileServiceClient;
  @MockBean
  private WorkflowServiceClient workflowServiceClient;
  @Captor
  private ArgumentCaptor<StartWorkflowRequest> startWorkflowRequestCaptor;

  @Test
  public void shouldPassIngestionFlow() throws Exception {

    // given
    HttpHeaders headers = new HttpHeaders();
    headers.add(AUTHORIZATION, TEST_AUTH);
    headers.add(Headers.PARTITION, PARTITION);

    SubmitRequest request = SubmitRequest.builder().dataType(DataType.WELL_LOG).fileId(FILE_ID)
        .build();

    FileLocationResponse fileLocationResponse = FileLocationResponse.builder().driver(GCP)
        .location(TEST_FILE_LOCATION).build();
    Response fileResponse = Response.builder()
        .body(mapper.writeValueAsString(fileLocationResponse), StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(HttpStatus.OK.value()).build();

    given(fileServiceClient.getFileLocation(eq(TEST_AUTH), eq(PARTITION),
        eq(FileLocationRequest.builder().fileID(FILE_ID).build())))
        .willReturn(fileResponse);

    StartWorkflowResponse startWorkflowResponse = StartWorkflowResponse.builder()
        .workflowId(WORKFLOW_ID).build();

    Response workflowResponse = Response.builder()
        .body(mapper.writeValueAsString(startWorkflowResponse), StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(HttpStatus.OK.value()).build();

    given(workflowServiceClient.startWorkflow(eq(TEST_AUTH), eq(PARTITION), any()))
        .willReturn(workflowResponse);

    // when
    ResponseEntity result = (ResponseEntity) mockMvc
        .perform(MockMvcRequestBuilders.post("/")
            .headers(headers)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn().getAsyncResult();

    // then
    SubmitResponse response = (SubmitResponse) result.getBody();
    then(Objects.requireNonNull(response).getWorkflowId()).isEqualTo(WORKFLOW_ID);

    verify(workflowServiceClient).startWorkflow(any(), any(), startWorkflowRequestCaptor.capture());
    then(startWorkflowRequestCaptor.getValue()).satisfies(workflowRequest -> {
      then(workflowRequest.getDataType()).isEqualTo(DataType.WELL_LOG);
      then(workflowRequest.getWorkflowType()).isEqualTo(WorkflowType.INGEST);
    });
  }

  @Test
  public void shouldFailIngestionFlowInvalidJson() throws Exception {

    // given
    HttpHeaders headers = new HttpHeaders();

    // when
    MvcResult mvcResult = mockMvc
        .perform(MockMvcRequestBuilders.post("/")
            .headers(headers)
            .content("{\"test\";\"test\"}"))
        .andExpect(status().isBadRequest()).andReturn();

    // then
    then(Objects.requireNonNull(mvcResult.getResolvedException()).getMessage())
        .contains("Cannot convert JSON {\"test\";\"test\"}");
  }

}
