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
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.ingest.config.ObjectMapperConfig;
import org.opengroup.osdu.ingest.model.Headers;
import org.opengroup.osdu.ingest.model.type.file.OsduFile;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class WorkflowPayloadServiceTest {

  private static final String FILE_ID = "file-id";

  private ObjectMapper mapper;

  @Mock
  private OsduRecordHelper osduRecordHelper;
  @Mock
  private FileIntegrationService fileIntegrationService;

  WorkflowPayloadService workflowPayloadService;

  @BeforeEach
  void setUp() {
    ObjectMapperConfig objectMapperConfig = new ObjectMapperConfig();
    mapper = objectMapperConfig.objectMapper();
    workflowPayloadService = new WorkflowPayloadService(mapper, osduRecordHelper,
        fileIntegrationService);
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

    OsduFile osduFile = OsduFile.builder()
        .resourceTypeID("resource-type-id")
        .resourceID("resource-id").build();
    given(osduRecordHelper.populateOsduRecord(eq("location"))).willReturn(osduFile);

    // when
    Map<String, Object> context = workflowPayloadService.getContext(FILE_ID, headers);

    // then
    then(context.get("kind")).isEqualTo("partition-id:ingestion-test:wellbore:1.0.1");
    then(context.get("legal")).isEqualTo("legal");
    then((String) context.get("id")).contains("partition-id:ingestion-test:file-file-id");
    then(context.get("acl")).isEqualTo("acl");

    then((Map<String, Object>) context.get("data")).satisfies(data -> {
      then((Map<String, Object>) data.get("osdu")).satisfies(osdu -> {
        then(osdu.get("ResourceID")).isEqualTo("resource-id");
        then(osdu.get("ResourceTypeID")).isEqualTo("resource-type-id");
      });
    });
  }

}
