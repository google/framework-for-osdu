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
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
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
public class WorkflowMvcTest {

  private static final String TEST_AUTH = "test-auth";
  private static final String PARTITION = "partition";
  private static final String WELL_LOG_DATA_TYPE = "WELL_LOG";
  private static final String UNAUTHORIZED_MSG = "The user is not authorized to perform this action";
  private static final String TEST_DAG = "test-dag";

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

  @Captor
  private ArgumentCaptor<WorkflowStatus> workflowStatusCaptor;

  @Test
  public void shouldPassStartWorkflowEntireFlow() throws Exception {

    // given
    HttpHeaders headers = getHttpHeaders();
    Map<String, Object> context = new HashMap<>();
    context.put("key", "value");
    StartWorkflowRequest request = StartWorkflowRequest.builder()
        .dataType(WELL_LOG_DATA_TYPE)
        .context(context)
        .workflowType(WorkflowType.OSDU)
        .build();

    given(ingestionStrategyRepository
        .findByWorkflowTypeAndDataTypeAndUserId(eq(WorkflowType.OSDU), eq(WELL_LOG_DATA_TYPE),
            any())).willReturn(IngestionStrategy.builder().dagName(TEST_DAG).build());

    given(submitIngestService.submitIngest(eq(TEST_DAG), eq(context)))
        .willReturn(Boolean.TRUE);

    given(workflowStatusRepository.saveWorkflowStatus(workflowStatusCaptor.capture()))
        .will(returnsFirstArg());
    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/startWorkflow")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .headers(headers)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn();

    // then
    StartWorkflowResponse startWorkflowResponse = mapper
        .readValue(mvcResult.getResponse().getContentAsString(), StartWorkflowResponse.class);
    then(startWorkflowResponse.getWorkflowId()).isNotNull();
    verify(workflowStatusRepository).saveWorkflowStatus(workflowStatusCaptor.capture());
    then(workflowStatusCaptor.getValue()).satisfies(status -> {
      then(status.getWorkflowStatusType()).isEqualTo(WorkflowStatusType.SUBMITTED);
      then(status.getWorkflowId()).isEqualTo(startWorkflowResponse.getWorkflowId());
      then(status.getAirflowRunId()).isNotNull();
    });
  }

  @Test
  public void shouldFailStartWorkflowInvalidJson() throws Exception {

    // given
    HttpHeaders headers = new HttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    MvcResult mvcResult = mockMvc.perform(
        post("/startWorkflow").contentType(MediaType.APPLICATION_JSON)
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
  public void shouldFailStartWorkflowUnauthorized() throws Exception {

    // given
    HttpHeaders headers = getHttpHeaders();
    StartWorkflowRequest request = StartWorkflowRequest.builder()
        .dataType(WELL_LOG_DATA_TYPE)
        .context(new HashMap<>())
        .workflowType(WorkflowType.OSDU)
        .build();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willThrow(AppException.createUnauthorized("test: viewer"));

    // when
    mockMvc.perform(
        post("/startWorkflow")
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
