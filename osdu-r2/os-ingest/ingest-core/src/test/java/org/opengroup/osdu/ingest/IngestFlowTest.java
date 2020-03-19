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
import static org.opengroup.osdu.ingest.ResourceUtils.getResource;
import static org.opengroup.osdu.ingest.TestUtils.getFeignRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.ingest.client.IFileServiceClient;
import org.opengroup.osdu.ingest.client.IWorkflowServiceClient;
import org.opengroup.osdu.ingest.model.SchemaData;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.opengroup.osdu.ingest.provider.interfaces.ISchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ExtendWith(SpringExtension.class)
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

  private static final String LOAD_MANIFEST_SCHEMA_TITLE = "WorkProductLoadManifestStagedFiles";
  private static final String LOAD_MANIFEST_SCHEMA_PATH = "3-schemas/WorkProductLoadManifestStagedFiles.json";
  private static final String WELL_LOG_WP_PATH = "4-instances/4-work-products/load_log_8821_p0105_1980_comp_las.json";
  private static final String WELL_LOG_DATA_TYPE = "WELL_LOG";
  private static final String OSDU_DATA_TYPE = "osdu";
  private static final String UNAUTHORIZED_MSG = "The user is not authorized to perform this action";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private IFileServiceClient fileServiceClient;
  @MockBean
  private IWorkflowServiceClient workflowServiceClient;
  @MockBean
  private ISchemaRepository schemaRepository;
  @MockBean
  private IAuthorizationService authorizationService;

  @Captor
  private ArgumentCaptor<StartWorkflowRequest> startWorkflowRequestCaptor;

  @Test
  public void shouldPassIngestionFlow() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();
    SubmitRequest request = SubmitRequest.builder()
        .dataType(WELL_LOG_DATA_TYPE)
        .fileId(FILE_ID)
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

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    mockMvc.perform(
        post("/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.WorkflowID").value(WORKFLOW_ID))
        .andReturn();

    // then
    verify(workflowServiceClient).startWorkflow(any(), any(), startWorkflowRequestCaptor.capture());
    then(startWorkflowRequestCaptor.getValue()).satisfies(workflowRequest -> {
      then(workflowRequest.getDataType()).isEqualTo(WELL_LOG_DATA_TYPE);
      then(workflowRequest.getWorkflowType()).isEqualTo(WorkflowType.INGEST);
    });
  }

  @Test
  public void shouldFailIngestionFlowInvalidJson() throws Exception {
    // given
    HttpHeaders headers = new HttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content("{\"test\";\"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();

    // then
    then(Objects.requireNonNull(mvcResult.getResolvedException()).getMessage())
        .contains("JSON parse error");
  }

  @Test
  public void shouldFailIngestionFlowUnauthorized() throws Exception {
    // given
    HttpHeaders headers = new HttpHeaders();
    SubmitRequest request = SubmitRequest.builder()
        .dataType(WELL_LOG_DATA_TYPE)
        .fileId(FILE_ID)
        .build();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willThrow(AppException.createUnauthorized("test: viewer"));

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MSG))
        .andReturn();

    // then
    verify(authorizationService).authorizeAny(any(), eq("service.storage.creator"));
  }

  @Test
  public void shouldSuccessfullySubmitOsduManifest() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();

    SchemaData schemaData = SchemaData.builder()
        .title(LOAD_MANIFEST_SCHEMA_TITLE)
        .schema(mapper.readTree(getResource(LOAD_MANIFEST_SCHEMA_PATH)))
        .build();
    given(schemaRepository.findByTitle(LOAD_MANIFEST_SCHEMA_TITLE)).willReturn(
        schemaData);
    StartWorkflowResponse startWorkflowResponse = StartWorkflowResponse.builder()
        .workflowId(WORKFLOW_ID).build();

    Response workflowResponse = Response.builder()
        .body(mapper.writeValueAsString(startWorkflowResponse), StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(HttpStatus.OK.value()).build();
    given(workflowServiceClient.startWorkflow(eq(TEST_AUTH), eq(PARTITION), any()))
        .willReturn(workflowResponse);

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    mockMvc.perform(
        post("/submitWithManifest")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content(getResource(WELL_LOG_WP_PATH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.WorkflowID").value(WORKFLOW_ID))
        .andReturn();

    // then
    verify(workflowServiceClient).startWorkflow(any(), any(), startWorkflowRequestCaptor.capture());
    then(startWorkflowRequestCaptor.getValue()).satisfies(workflowRequest -> {
      then(workflowRequest.getDataType()).isEqualTo(OSDU_DATA_TYPE);
      then(workflowRequest.getWorkflowType()).isEqualTo(WorkflowType.OSDU);
    });
  }

  @Test
  public void shouldFailSubmitOsduManifest() throws Exception {
    // given
    HttpHeaders headers = new HttpHeaders();

    SchemaData schemaData = SchemaData.builder()
        .title(LOAD_MANIFEST_SCHEMA_TITLE)
        .schema(mapper.readTree(getResource(LOAD_MANIFEST_SCHEMA_PATH)))
        .build();
    given(schemaRepository.findByTitle(LOAD_MANIFEST_SCHEMA_TITLE)).willReturn(
        schemaData);

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/submitWithManifest")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content("{}"))
        .andExpect(status().isBadRequest()).andReturn();

    mvcResult.getResponse();
    // then
    then(Objects.requireNonNull(mvcResult.getResolvedException()).getMessage())
        .containsPattern("Failed to validate json from manifest (.*), validation result is (.*)");
  }

  @Test
  public void shouldFailSubmitOsduManifestUnauthorized() throws Exception {
    // given
    HttpHeaders headers = new HttpHeaders();
    SubmitRequest request = SubmitRequest.builder()
        .dataType(WELL_LOG_DATA_TYPE)
        .fileId(FILE_ID)
        .build();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willThrow(AppException.createUnauthorized("test: viewer"));

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/submitWithManifest")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content(getResource(WELL_LOG_WP_PATH)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MSG))
        .andReturn();

    // then
    verify(authorizationService).authorizeAny(any(), eq("service.storage.creator"));
  }

  private HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(DpsHeaders.AUTHORIZATION, TEST_AUTH);
    headers.add(DpsHeaders.DATA_PARTITION_ID, PARTITION);
    return headers;
  }

  @TestConfiguration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  public static class TestSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

      http.httpBasic().disable()
          .csrf().disable();  //disable default authN. AuthN handled by endpoints proxy
    }

  }

}
