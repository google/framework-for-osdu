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

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.ingest.client.IWorkflowServiceClient;
import org.opengroup.osdu.ingest.exception.ServerErrorException;
import org.opengroup.osdu.ingest.provider.interfaces.IWorkflowIntegrationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowIntegrationServiceImpl implements IWorkflowIntegrationService {

  final IWorkflowServiceClient workflowServiceClient;
  final ObjectMapper objectMapper;

  @Override
  public String submitIngestToWorkflowService(WorkflowType workflowType, String dataType,
      Map<String, Object> context,
      DpsHeaders commonHeaders) {

    StartWorkflowRequest request = StartWorkflowRequest.builder()
        .workflowType(workflowType)
        .dataType(dataType)
        .context(context).build();

    log.debug("Send start workflow request to workflow service, request - {}", request);
    try (Response response = startWorkflow(commonHeaders, request)) {
      StartWorkflowResponse startWorkflowResponse = objectMapper
          .readValue(response.body().asInputStream(), StartWorkflowResponse.class);

      log.debug("Receive start workflow response from workflow service, response - {}",
          startWorkflowResponse);

      if (startWorkflowResponse.getWorkflowId() == null) {
        throw new ServerErrorException("No workflow id in workflow service response");
      }

      return startWorkflowResponse.getWorkflowId();
    } catch (IOException exception) {
      throw new ServerErrorException("Exception in start workflow request", exception);
    }
  }

  private Response startWorkflow(DpsHeaders commonHeaders, StartWorkflowRequest request) {
    return workflowServiceClient.startWorkflow(commonHeaders.getAuthorization(),
        commonHeaders.getPartitionIdWithFallbackToAccountId(), request);
  }
}
