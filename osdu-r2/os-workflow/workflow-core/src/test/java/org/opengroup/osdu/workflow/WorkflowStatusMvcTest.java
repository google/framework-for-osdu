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

package org.opengroup.osdu.workflow;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.model.GetStatusResponse;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.UpdateStatusResponse;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IIngestionStrategyRepository;
import org.opengroup.osdu.workflow.provider.interfaces.ISubmitIngestService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
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
public class WorkflowStatusMvcTest {

  private static final String WORKFLOW_ID = "workflow-id";
  private static final String TEST_AUTH = "test-auth";
  private static final String PARTITION = "partition";
  private static final String UNAUTHORIZED_MSG = "The user is not authorized to perform this action";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private IIngestionStrategyRepository ingestionStrategyRepository;
  @MockBean
  private ISubmitIngestService submitIngestService;
  @MockBean
  private IWorkflowStatusRepository workflowStatusRepository;
  @MockBean
  private IAuthorizationService authorizationService;

  @Test
  public void shouldPassGetWorkflowStatusFlow() throws Exception {

    // given
    HttpHeaders headers = getHttpHeaders();

    GetStatusRequest request = GetStatusRequest.builder().workflowId(WORKFLOW_ID).build();
    WorkflowStatus status = WorkflowStatus.builder().workflowStatusType(WorkflowStatusType.RUNNING)
        .workflowId(WORKFLOW_ID)
        .airflowRunId("airflow-id")
        .submittedAt(new Date())
        .build();

    given(workflowStatusRepository
        .findWorkflowStatus(eq(WORKFLOW_ID))).willReturn(status);

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/getStatus")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn();

    // then
    GetStatusResponse startWorkflowResponse = mapper
        .readValue(mvcResult.getResponse().getContentAsString(), GetStatusResponse.class);
    then(startWorkflowResponse.getWorkflowStatusType()).isEqualTo(status.getWorkflowStatusType());
  }

  @Test
  public void shouldThrowNotFoundIfThereIsNoStatus() throws Exception {

    // given
    HttpHeaders headers = getHttpHeaders();
    GetStatusRequest request = GetStatusRequest.builder().workflowId(WORKFLOW_ID).build();

    given(workflowStatusRepository
        .findWorkflowStatus(eq(WORKFLOW_ID))).willReturn(null);

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    mockMvc.perform(
        post("/getStatus")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andReturn();
  }


  @Test
  public void shouldFailGetStatusInvalidJson() throws Exception {

    // given
    HttpHeaders headers = new HttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/getStatus")
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
  public void shouldFailGetStatusUnauthorized() throws Exception {

    // given
    HttpHeaders headers = getHttpHeaders();

    GetStatusRequest request = GetStatusRequest.builder().workflowId(WORKFLOW_ID).build();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willThrow(AppException.createUnauthorized("test: viewer"));

    // when
    mockMvc.perform(
        post("/getStatus")
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
  public void shouldPassUpdateWorkflowStatusFlow() throws Exception {

    // given
    HttpHeaders headers = getHttpHeaders();

    UpdateStatusRequest request = UpdateStatusRequest.builder()
        .workflowId(WORKFLOW_ID)
        .workflowStatusType(WorkflowStatusType.RUNNING).build();
    WorkflowStatus status = WorkflowStatus.builder()
        .workflowStatusType(WorkflowStatusType.SUBMITTED)
        .workflowId(WORKFLOW_ID)
        .airflowRunId("airflow-id")
        .submittedAt(new Date())
        .build();

    WorkflowStatus updatedStatus = WorkflowStatus.builder()
        .workflowStatusType(WorkflowStatusType.RUNNING)
        .workflowId(WORKFLOW_ID)
        .airflowRunId("airflow-id")
        .submittedAt(new Date())
        .build();

    given(workflowStatusRepository.findWorkflowStatus(eq(WORKFLOW_ID))).willReturn(status);

    given(workflowStatusRepository
        .updateWorkflowStatus(eq(status.getWorkflowId()), eq(WorkflowStatusType.RUNNING)))
        .willReturn(updatedStatus);

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/updateStatus")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn();

    // then
    UpdateStatusResponse response = mapper
        .readValue(mvcResult.getResponse().getContentAsString(), UpdateStatusResponse.class);
    then(response.getWorkflowStatusType()).isEqualTo(WorkflowStatusType.RUNNING);
    then(response.getWorkflowId()).isEqualTo(WORKFLOW_ID);
  }


  @Test
  public void shouldFailUpdateStatusInvalidJson() throws Exception {

    // given
    HttpHeaders headers = new HttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/updateStatus")
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
  public void shouldFailUpdateStatusUnauthorized() throws Exception {

    // given
    HttpHeaders headers = getHttpHeaders();

    UpdateStatusRequest request = UpdateStatusRequest.builder()
        .workflowId(WORKFLOW_ID)
        .workflowStatusType(WorkflowStatusType.RUNNING).build();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willThrow(AppException.createUnauthorized("test: viewer"));

    // when
    mockMvc.perform(
        post("/updateStatus")
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
