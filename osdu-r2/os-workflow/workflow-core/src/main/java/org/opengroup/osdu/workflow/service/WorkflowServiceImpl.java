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

package org.opengroup.osdu.workflow.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IIngestionStrategyService;
import org.opengroup.osdu.workflow.provider.interfaces.ISubmitIngestService;
import org.opengroup.osdu.workflow.provider.interfaces.IValidationService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements IWorkflowService {

  final IValidationService validationService;
  final IIngestionStrategyService ingestionStrategyService;
  final ISubmitIngestService submitIngestService;
  final IWorkflowStatusRepository workflowStatusRepository;

  @Override
  public StartWorkflowResponse startWorkflow(StartWorkflowRequest request, DpsHeaders headers) {
    log.debug("Start Workflow with payload - {}", request);

    validationService.validateStartWorkflowRequest(request);

    String userId = headers.getUserEmail();

    String strategyName = ingestionStrategyService.determineStrategy(request.getWorkflowType(),
        request.getDataType(), userId);

    String workflowId = UUID.randomUUID().toString();
    String airflowRunId = UUID.randomUUID().toString();

    submitIngestService.submitIngest(strategyName, request.getContext());

    workflowStatusRepository.saveWorkflowStatus(WorkflowStatus.builder()
        .workflowId(workflowId)
        .airflowRunId(airflowRunId)
        .workflowStatusType(WorkflowStatusType.SUBMITTED)
        .build());

    return StartWorkflowResponse.builder().workflowId(workflowId).build();
  }

}
