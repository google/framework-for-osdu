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

package org.opengroup.osdu.workflow.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@RestController
@RequestScope
@Validated
@RequiredArgsConstructor
public class WorkflowApi {

  final DpsHeaders headers;
  final IWorkflowService workflowService;

  @PostMapping("/startWorkflow")
  @PreAuthorize("@authorizationFilter.hasPermission('" + StorageRole.CREATOR + "')")
  public StartWorkflowResponse startWorkflow(@RequestBody StartWorkflowRequest request) {
    log.debug("Start Workflow request received : {}", request);
    StartWorkflowResponse response = workflowService.startWorkflow(request, headers);
    log.debug("Start Workflow result ready : {}", response);
    return response;
  }
}
