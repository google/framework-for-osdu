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
import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.ingest.mapper.HeadersMapper;
import org.opengroup.osdu.ingest.model.CreateRecordPayload;
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
  final FileIntegrationService fileIntegrationService;
  final WorkflowIntegrationService workflowIntegrationService;
  final ValidationService validationService;
  final ObjectMapper objectMapper;

  @Override
  public SubmitResponse submit(SubmitRequest request, MessageHeaders messageHeaders) {
    log.debug("Submit request with payload - {} and headers - {}", request, messageHeaders);

    Headers headers = headersMapper.toHeaders(messageHeaders);

    authenticationService.checkAuthentication(headers.getAuthorizationToken(),
        headers.getPartitionID());
    validationService.validateSubmitRequest(request);

    String fileId = request.getFileId();
    FileLocationResponse fileLocation = fileIntegrationService.getFileInfo(fileId, headers);

    Map<String, Object> osduRecord = populateOsduRecord(fileId, fileLocation.getLocation());
    Map<String, Object> context = populateContext(fileId, headers, osduRecord);

    String workflowId = workflowIntegrationService
        .submitIngestToWorkflowService(request.getDataType(), context, headers);

    SubmitResponse response = SubmitResponse.builder().workflowId(workflowId).build();
    log.debug("Submit response - {}", response);
    return response;
  }

  private Map<String, Object> populateOsduRecord(String fileId, String fileLocation) {
    //TODO construct object that matches osdu file record schema
    Map<String, Object> record = new HashMap<>();
    record.put("fileId", fileId);
    record.put("fileLocation", fileLocation);

    return record;
  }


  private Map<String, Object> populateContext(String fileId, Headers headers,
      Map<String, Object> data) {

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
        .data(data).build();

    return objectMapper
        .convertValue(createRecordPayload, new TypeReference<HashMap<String, Object>>() {
        });
  }
}
