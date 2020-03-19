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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.ingest.model.SubmitResponse;
import org.opengroup.osdu.ingest.model.WorkProductLoadManifest;
import org.opengroup.osdu.ingest.model.property.DataTypeProperties;
import org.opengroup.osdu.ingest.provider.interfaces.IOsduSubmitService;
import org.opengroup.osdu.ingest.provider.interfaces.IValidationService;
import org.opengroup.osdu.ingest.provider.interfaces.IWorkflowIntegrationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OsduSubmitServiceImpl implements IOsduSubmitService {

  final IWorkflowIntegrationService workflowIntegrationService;
  final IValidationService validationService;
  final ObjectMapper objectMapper;
  final DataTypeProperties dataTypeProperties;

  @Override
  public SubmitResponse submit(WorkProductLoadManifest manifest, DpsHeaders headers) {
    log.debug("Submit manifest with payload - {} and headers - {}", manifest, headers);

    validationService.validateManifest(manifest);

    Map<String, Object> context = populateContext(manifest);

    String workflowId = workflowIntegrationService.submitIngestToWorkflowService(WorkflowType.OSDU,
        dataTypeProperties.getLoadManifestType(), context, headers);

    SubmitResponse response = SubmitResponse.builder().workflowId(workflowId).build();
    log.debug("Submit manifest response - {}", response);
    return response;
  }

  private Map<String, Object> populateContext(WorkProductLoadManifest manifest) {
    return objectMapper.convertValue(manifest, new TypeReference<HashMap<String, Object>>() {
    });
  }

}
