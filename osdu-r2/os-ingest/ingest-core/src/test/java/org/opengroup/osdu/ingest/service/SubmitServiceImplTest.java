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

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.opengroup.osdu.ingest.model.Headers.AUTHORIZATION;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.ingest.mapper.HeadersMapper;
import org.opengroup.osdu.ingest.model.Headers;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.opengroup.osdu.ingest.model.SubmitResponse;
import org.opengroup.osdu.ingest.validation.ValidationService;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class SubmitServiceImplTest {

  private static final String FILE_ID = "file-id";
  private static final String PARTITION = "partition";
  private static final String AUTH_TOKEN = "auth-token";
  private static final String WORKFLOW_ID = "workflow-id";

  @Spy
  private HeadersMapper headersMapper = Mappers.getMapper(HeadersMapper.class);
  @Mock
  private AuthenticationService authenticationService;
  @Mock
  private WorkflowIntegrationService workflowIntegrationService;
  @Mock
  private ValidationService validationService;
  @Mock
  private WorkflowPayloadService workflowPayloadService;

  @Captor
  ArgumentCaptor<Map<String, Object>> contextCaptor;

  SubmitServiceImpl submitServiceImpl;

  @BeforeEach
  void setUp() {
    submitServiceImpl = new SubmitServiceImpl(headersMapper,
        authenticationService,
        workflowIntegrationService,
        validationService,
        workflowPayloadService);
  }

  @Test
  void shouldSubmitIngestToWorkflowService() {

    // given
    SubmitRequest request = SubmitRequest.builder()
        .dataType(DataType.WELL_LOG)
        .fileId(FILE_ID).build();

    Map<String, Object> headersMap = new HashMap<>();
    headersMap.put(AUTHORIZATION, AUTH_TOKEN);
    headersMap.put(PARTITION, PARTITION);
    MessageHeaders messageHeaders = new MessageHeaders(headersMap);

    given(workflowPayloadService.getContext(eq(FILE_ID), any()))
        .willReturn(Collections.singletonMap("key", "value"));

    given(workflowIntegrationService.submitIngestToWorkflowService(eq(DataType.WELL_LOG)
        , any(), any(Headers.class))).willReturn(WORKFLOW_ID);

    // when
    SubmitResponse submitResponse = submitServiceImpl.submit(request, messageHeaders);

    // then
    then(submitResponse.getWorkflowId()).isEqualTo(WORKFLOW_ID);

    verify(workflowIntegrationService)
        .submitIngestToWorkflowService(any(), contextCaptor.capture(), any());
    then(contextCaptor.getValue()).containsEntry("key", "value");
  }

}
