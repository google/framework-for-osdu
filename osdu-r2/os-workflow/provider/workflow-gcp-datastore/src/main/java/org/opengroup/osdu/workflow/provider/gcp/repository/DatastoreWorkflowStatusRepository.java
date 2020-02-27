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

package org.opengroup.osdu.workflow.provider.gcp.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusNotFoundException;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusNotUpdatedException;
import org.opengroup.osdu.workflow.provider.gcp.mapper.WorkflowStatusMapper;
import org.opengroup.osdu.workflow.provider.gcp.model.WorkflowStatusEntity;
import org.opengroup.osdu.workflow.provider.interfaces.WorkflowStatusRepository;
import org.springframework.stereotype.Repository;

// TODO Will be moved to registry service
@Repository
@Slf4j
@RequiredArgsConstructor
public class DatastoreWorkflowStatusRepository implements WorkflowStatusRepository {

  final WorkflowStatusMapper workflowStatusMapper;
  final WorkflowStatusEntityRepository workflowStatusEntityRepository;

  @Override
  public WorkflowStatus findWorkflowStatus(String workflowId) {
    log.debug("Requesting workflow status by workflow id - {}", workflowId);
    WorkflowStatusEntity entity = workflowStatusEntityRepository.findByWorkflowId(workflowId);
    WorkflowStatus workflowStatus = workflowStatusMapper.toWorkflowStatus(entity);
    log.debug("Found workflow status : {}", workflowStatus);
    return workflowStatus;
  }

  @Override
  public WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus) {
    log.info("Saving workflow status  location : {}", workflowStatus);
    WorkflowStatusEntity saved = workflowStatusEntityRepository
        .save(workflowStatusMapper.toEntity(workflowStatus));
    log.info("Fetch saved workflow status : {}", saved);
    return workflowStatusMapper.toWorkflowStatus(saved);
  }

  @Override
  public WorkflowStatus updateWorkflowStatus(String workflowId,
      WorkflowStatusType workflowStatusType) {
    log.info("Update workflow status  for workflow id: {}, new status: {}", workflowId,
        workflowStatusType);
    WorkflowStatusEntity entity = workflowStatusEntityRepository.findByWorkflowId(workflowId);
    if (entity == null) {
      throw new WorkflowStatusNotFoundException(
          String.format("Workflow status for Workflow id: %s not found", workflowId));
    }

    if (entity.getWorkflowStatusType().equals(workflowStatusType.name())) {
      throw new WorkflowStatusNotUpdatedException(String.format(
          "Workflow status for workflow id: %s already has status:%s and can not be updated",
          workflowId, workflowStatusType));
    }

    entity.setWorkflowStatusType(workflowStatusType.name());
    WorkflowStatusEntity saved = workflowStatusEntityRepository.save(entity);
    log.info("Fetch updated workflow status : {}", saved);
    return workflowStatusMapper.toWorkflowStatus(saved);
  }

}
