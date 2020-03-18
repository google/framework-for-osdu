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

package org.opengroup.osdu.ingest.service;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
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
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.opengroup.osdu.ingest.model.SubmitResponse;
import org.opengroup.osdu.ingest.provider.interfaces.IValidationService;
import org.opengroup.osdu.ingest.provider.interfaces.IWorkflowIntegrationService;
import org.opengroup.osdu.ingest.provider.interfaces.IWorkflowPayloadService;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class SubmitServiceImplTest {

  private static final String FILE_ID = "file-id";
  private static final String PARTITION = "partition";
  private static final String AUTH_TOKEN = "auth-token";
  private static final String WORKFLOW_ID = "workflow-id";

  @Mock
  private IWorkflowIntegrationService workflowIntegrationService;
  @Mock
  private IValidationService validationService;
  @Mock
  private IWorkflowPayloadService workflowPayloadService;

  @Captor
  ArgumentCaptor<Map<String, Object>> contextCaptor;

  SubmitServiceImpl submitServiceImpl;

  @BeforeEach
  void setUp() {
    submitServiceImpl = new SubmitServiceImpl(
        workflowIntegrationService,
        validationService,
        workflowPayloadService);
  }

  @Test
  void shouldSubmitIngestToWorkflowService() {

    // given
    SubmitRequest request = SubmitRequest.builder()
        .dataType("WELL_LOG")
        .fileId(FILE_ID).build();

    Map<String, String> headersMap = new HashMap<>();
    headersMap.put(DpsHeaders.AUTHORIZATION, AUTH_TOKEN);
    headersMap.put(PARTITION, PARTITION);
    DpsHeaders headers = DpsHeaders.createFromMap(headersMap);

    given(workflowPayloadService.getContext(eq(FILE_ID), any()))
        .willReturn(Collections.singletonMap("key", "value"));

    given(workflowIntegrationService
        .submitIngestToWorkflowService(eq(WorkflowType.INGEST), eq("WELL_LOG")
            , any(), any(DpsHeaders.class))).willReturn(WORKFLOW_ID);

    // when
    SubmitResponse submitResponse = submitServiceImpl.submit(request, headers);

    // then
    then(submitResponse.getWorkflowId()).isEqualTo(WORKFLOW_ID);

    verify(workflowIntegrationService)
        .submitIngestToWorkflowService(any(), any(), contextCaptor.capture(), any());
    then(contextCaptor.getValue()).containsEntry("key", "value");
  }

}
