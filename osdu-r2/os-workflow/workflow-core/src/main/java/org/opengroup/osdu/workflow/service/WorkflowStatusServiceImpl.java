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

import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.Headers;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.mapper.HeadersMapper;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.model.GetStatusResponse;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.UpdateStatusResponse;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.provider.interfaces.AuthenticationService;
import org.opengroup.osdu.workflow.provider.interfaces.ValidationService;
import org.opengroup.osdu.workflow.provider.interfaces.WorkflowStatusRepository;
import org.opengroup.osdu.workflow.provider.interfaces.WorkflowStatusService;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowStatusServiceImpl implements WorkflowStatusService {

  @Named
  final HeadersMapper headersMapper;
  final AuthenticationService authenticationService;
  final ValidationService validationService;
  final WorkflowStatusRepository workflowStatusRepository;

  @Override
  public GetStatusResponse getWorkflowStatus(GetStatusRequest request,
      MessageHeaders messageHeaders) {
    log.debug("Request get workflow status with parameters : {}, and headers, {}", request,
        messageHeaders);

    Headers headers = headersMapper.toHeaders(messageHeaders);

    authenticationService.checkAuthentication(headers.getAuthorizationToken(),
        headers.getPartitionID());
    validationService.validateGetStatusRequest(request);

    WorkflowStatus workflowStatus = workflowStatusRepository
        .findWorkflowStatus(request.getWorkflowId());

    if (workflowStatus == null) {
      throw new WorkflowNotFoundException(
          String.format("Workflow for workflow id - %s not found", request.getWorkflowId()));
    }

    GetStatusResponse response = GetStatusResponse.builder()
        .workflowStatusType(workflowStatus.getWorkflowStatusType()).build();

    log.debug("Get workflow status result: {}", response);
    return response;
  }

  @Override
  public UpdateStatusResponse updateWorkflowStatus(UpdateStatusRequest request,
      MessageHeaders messageHeaders) {
    log.debug("Request update workflow status with parameters : {}, and headers, {}", request,
        messageHeaders);

    Headers headers = headersMapper.toHeaders(messageHeaders);

    authenticationService.checkAuthentication(headers.getAuthorizationToken(),
        headers.getPartitionID());
    validationService.validateUpdateStatusRequest(request);

    WorkflowStatus workflowStatus = workflowStatusRepository
        .updateWorkflowStatus(request.getWorkflowId(), request.getWorkflowStatusType());


    UpdateStatusResponse response = UpdateStatusResponse.builder()
        .workflowId(workflowStatus.getWorkflowId())
        .workflowStatusType(workflowStatus.getWorkflowStatusType()).build();

    log.debug("Get workflow status result: {}", response);
    return response;
  }

}
