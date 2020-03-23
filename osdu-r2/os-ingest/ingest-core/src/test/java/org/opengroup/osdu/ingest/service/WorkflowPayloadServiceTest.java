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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.model.property.WorkflowProperties;
import org.opengroup.osdu.ingest.provider.interfaces.IFileIntegrationService;
import org.opengroup.osdu.ingest.provider.interfaces.IWorkflowPayloadService;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class WorkflowPayloadServiceTest {

  private static final String FILE_ID = "file-id";

  private WorkflowProperties workflowProperties = WorkflowProperties.builder()
      .appKey("test-app-key")
      .build();

  @Mock
  private IFileIntegrationService fileIntegrationService;

  private IWorkflowPayloadService workflowPayloadService;

  @BeforeEach
  void setUp() {
    workflowPayloadService = new WorkflowPayloadServiceImpl(workflowProperties,
        fileIntegrationService, new ObjectMapper());
  }

  @Test
  void shouldGenerateIngestContext() {

    // given
    Map<String, String> headersMap = new HashMap<>();
    headersMap.put(DpsHeaders.AUTHORIZATION, "test-auth");
    headersMap.put(DpsHeaders.DATA_PARTITION_ID, "partition-id");
    headersMap.put(DpsHeaders.ACL_HEADER, "acl");
    headersMap.put(DpsHeaders.LEGAL_TAGS, "legal");
    DpsHeaders headers = DpsHeaders.createFromMap(headersMap);

    FileLocationResponse fileLocation = FileLocationResponse.builder().location("location").build();
    given(fileIntegrationService.getFileInfo(eq(FILE_ID), eq(headers))).willReturn(fileLocation);

    // when
    Map<String, Object> context = workflowPayloadService.getContext(FILE_ID, headers);

    // then
    then(context.get("authorization")).isEqualTo("test-auth");
    then(context.get("legal-tags")).isEqualTo("legal");
    then(context.get("acl")).isEqualTo("acl");
    then(context.get("data-partition-id")).isEqualTo("partition-id");
    then(context.get("AppKey")).isEqualTo("test-app-key");
    then(((Map<String, Object>)context.get("data")).get("FileID")).isEqualTo(FILE_ID);
  }

}
