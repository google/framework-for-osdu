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

package org.opengroup.osdu.workflow.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.opengroup.osdu.core.common.model.Headers;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.workflow.mapper.HeadersMapper;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.AuthenticationService;
import org.opengroup.osdu.workflow.provider.interfaces.IngestionStrategyService;
import org.opengroup.osdu.workflow.provider.interfaces.SubmitIngestService;
import org.opengroup.osdu.workflow.provider.interfaces.ValidationService;
import org.opengroup.osdu.workflow.provider.interfaces.WorkflowService;
import org.opengroup.osdu.workflow.provider.interfaces.WorkflowStatusRepository;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

  @Named
  final HeadersMapper headersMapper;
  final AuthenticationService authenticationService;
  final ValidationService validationService;
  final IngestionStrategyService ingestionStrategyService;
  final SubmitIngestService submitIngestService;
  final WorkflowStatusRepository workflowStatusRepository;

  @Override
  public StartWorkflowResponse startWorkflow(StartWorkflowRequest request,
      MessageHeaders messageHeaders) {

    Headers headers = headersMapper.toHeaders(messageHeaders);

    authenticationService.checkAuthentication(headers.getAuthorizationToken(),
        headers.getPartitionID());
    validationService.validateStartWorkflowRequest(request);

    // TODO will be populated after authorization came
    String userId = null;

    String strategyName = ingestionStrategyService.determineStrategy(request.getWorkflowType(),
        request.getDataType(), userId);

    Map<String, Object> context = ObjectUtils.defaultIfNull(request.getContext(), new HashMap<>());

    String workflowId = UUID.randomUUID().toString();
    String airflowRunId = UUID.randomUUID().toString();

    submitIngestService.submitIngest(strategyName, context);

    workflowStatusRepository.saveWorkflowStatus(WorkflowStatus.builder()
        .workflowId(workflowId)
        .airflowRunId(airflowRunId)
        .workflowStatusType(WorkflowStatusType.SUBMITTED)
        .build());

    return StartWorkflowResponse.builder().workflowId(workflowId).build();
  }

}
