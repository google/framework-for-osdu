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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.ingest.model.CreateRecordPayload;
import org.opengroup.osdu.ingest.model.Headers;
import org.opengroup.osdu.ingest.model.type.file.OsduFile;
import org.opengroup.osdu.ingest.provider.interfaces.FileIntegrationService;
import org.opengroup.osdu.ingest.provider.interfaces.WorkflowPayloadService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowPayloadServiceImpl implements WorkflowPayloadService {

  final ObjectMapper objectMapper;
  final OsduRecordHelper osduRecordHelper;
  final FileIntegrationService fileIntegrationService;

  @Override
  public Map<String, Object> getContext(String fileId, Headers headers) {

    FileLocationResponse fileLocation = fileIntegrationService.getFileInfo(fileId, headers);

    OsduFile osduRecord = osduRecordHelper.populateOsduRecord(fileLocation.getLocation());

    return populateContext(fileId, headers, osduRecord);
  }

  private Map<String, Object> populateContext(String fileId, Headers headers, OsduFile osduFile) {
    String kind = String.format("%s:ingestion-test:wellbore:1.0.1", headers.getPartitionID());
    String suffix = LocalDateTime.now(Clock.systemUTC())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSSS"));
    String recordId = String
        .format("%s:ingestion-test:file-%s-%s", headers.getPartitionID(), fileId, suffix);

    CreateRecordPayload createRecordPayload = CreateRecordPayload.builder()
        .id(recordId)
        .kind(kind)
        .acl(headers.getAcl())
        .legal(headers.getLegalTags())
        .data(Collections.singletonMap("osdu", osduFile)).build();

    return objectMapper
        .convertValue(createRecordPayload, new TypeReference<HashMap<String, Object>>() {
        });
  }
}
