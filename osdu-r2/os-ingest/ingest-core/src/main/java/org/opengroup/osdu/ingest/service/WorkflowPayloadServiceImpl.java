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
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.ingest.model.IngestPayload;
import org.opengroup.osdu.ingest.model.property.WorkflowProperties;
import org.opengroup.osdu.ingest.provider.interfaces.IFileIntegrationService;
import org.opengroup.osdu.ingest.provider.interfaces.IWorkflowPayloadService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowPayloadServiceImpl implements IWorkflowPayloadService {

  final WorkflowProperties workflowProperties;
  final IFileIntegrationService fileIntegrationService;
  final ObjectMapper objectMapper;

  @Override
  public Map<String, Object> getContext(String fileId, DpsHeaders headers) {

    FileLocationResponse fileLocation = fileIntegrationService.getFileInfo(fileId, headers);

    Map<String, Object> data = new HashMap<>();
    data.put("FileID", fileId);

    IngestPayload ingestPayload = IngestPayload.builder()
        .acl(headers.getAcl())
        .legalTags(headers.getLegalTags())
        .authorizationToken(headers.getAuthorization())
        .partitionID(headers.getPartitionIdWithFallbackToAccountId())
        .appKey(workflowProperties.getAppKey())
        .data(data)
        .build();

    return objectMapper
        .convertValue(ingestPayload, new TypeReference<HashMap<String, Object>>() {
        });
  }

}
