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
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.ingest.mapper.HeadersMapper;
import org.opengroup.osdu.ingest.model.Headers;
import org.opengroup.osdu.ingest.model.SubmitResponse;
import org.opengroup.osdu.ingest.model.WorkProductLoadManifest;
import org.opengroup.osdu.ingest.provider.interfaces.AuthenticationService;
import org.opengroup.osdu.ingest.provider.interfaces.OsduSubmitService;
import org.opengroup.osdu.ingest.provider.interfaces.ValidationService;
import org.opengroup.osdu.ingest.provider.interfaces.WorkflowIntegrationService;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OsduSubmitServiceImpl implements OsduSubmitService {

  @Named
  final HeadersMapper headersMapper;
  final AuthenticationService authenticationService;
  final WorkflowIntegrationService workflowIntegrationService;
  final ValidationService validationService;
  final ObjectMapper objectMapper;

  @Override
  public SubmitResponse submit(WorkProductLoadManifest manifest, MessageHeaders messageHeaders) {
    log.debug("Submit manifest with payload - {} and headers - {}", manifest, messageHeaders);

    Headers headers = headersMapper.toHeaders(messageHeaders);

    authenticationService.checkAuthentication(headers.getAuthorizationToken(),
        headers.getPartitionID());
    validationService.validateManifest(manifest);

    Map<String, Object> context = populateContext(manifest);

    String workflowId = workflowIntegrationService
        .submitIngestToWorkflowService(WorkflowType.OSDU, DataType.OSDU, context, headers);

    SubmitResponse response = SubmitResponse.builder().workflowId(workflowId).build();
    log.debug("Submit manifest response - {}", response);
    return response;
  }

  private Map<String, Object> populateContext(WorkProductLoadManifest manifest) {
    return objectMapper.convertValue(manifest, new TypeReference<HashMap<String, Object>>() {
    });
  }

}
