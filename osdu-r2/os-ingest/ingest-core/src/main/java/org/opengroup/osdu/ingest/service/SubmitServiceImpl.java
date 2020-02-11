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

import java.util.Map;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.ingest.mapper.HeadersMapper;
import org.opengroup.osdu.ingest.model.Headers;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.opengroup.osdu.ingest.model.SubmitResponse;
import org.opengroup.osdu.ingest.validation.ValidationService;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitServiceImpl implements SubmitService {

  @Named
  final HeadersMapper headersMapper;
  final AuthenticationService authenticationService;
  final WorkflowIntegrationService workflowIntegrationService;
  final ValidationService validationService;
  final WorkflowPayloadService workflowPayloadService;

  @Override
  public SubmitResponse submit(SubmitRequest request, MessageHeaders messageHeaders) {
    log.debug("Submit request with payload - {} and headers - {}", request, messageHeaders);

    Headers headers = headersMapper.toHeaders(messageHeaders);

    authenticationService.checkAuthentication(headers.getAuthorizationToken(),
        headers.getPartitionID());
    validationService.validateSubmitRequest(request);

    Map<String, Object> context = workflowPayloadService.getContext(request.getFileId(), headers);

    String workflowId = workflowIntegrationService
        .submitIngestToWorkflowService(request.getDataType(), context, headers);

    SubmitResponse response = SubmitResponse.builder().workflowId(workflowId).build();
    log.debug("Submit response - {}", response);
    return response;
  }

}
