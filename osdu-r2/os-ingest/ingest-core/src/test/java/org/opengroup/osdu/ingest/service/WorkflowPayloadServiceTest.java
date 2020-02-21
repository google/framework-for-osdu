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

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.model.Headers;
import org.opengroup.osdu.ingest.provider.interfaces.FileIntegrationService;
import org.opengroup.osdu.ingest.provider.interfaces.WorkflowPayloadService;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class WorkflowPayloadServiceTest {

  private static final String FILE_ID = "file-id";

  @Mock
  private FileIntegrationService fileIntegrationService;

  WorkflowPayloadService workflowPayloadService;

  @BeforeEach
  void setUp() {
    workflowPayloadService = new WorkflowPayloadServiceImpl(fileIntegrationService);
  }

  @Test
  void shouldSubmitIngestToWorkflowService() {

    // given
    Headers headers = Headers.builder()
        .partitionID("partition-id")
        .acl("acl")
        .legalTags("legal")
        .build();
    FileLocationResponse fileLocation = FileLocationResponse.builder().location("location").build();
    given(fileIntegrationService.getFileInfo(eq(FILE_ID), eq(headers))).willReturn(fileLocation);

    // when
    Map<String, Object> context = workflowPayloadService.getContext(FILE_ID, headers);

    // then
    then(context.get("FileID")).isEqualTo(FILE_ID);
  }

}
