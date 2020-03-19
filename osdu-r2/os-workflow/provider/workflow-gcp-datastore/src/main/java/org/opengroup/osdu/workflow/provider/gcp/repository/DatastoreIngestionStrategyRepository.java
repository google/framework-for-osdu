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
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.gcp.mapper.IngestionStrategyMapper;
import org.opengroup.osdu.workflow.provider.gcp.model.IngestionStrategyEntity;
import org.opengroup.osdu.workflow.provider.interfaces.IngestionStrategyRepository;
import org.springframework.stereotype.Repository;

// TODO Will be moved to registry service
@Repository
@Slf4j
@RequiredArgsConstructor
public class DatastoreIngestionStrategyRepository implements IngestionStrategyRepository {

  final IngestionStrategyMapper ingestionStrategyMapper;
  final IngestionStrategyEntityRepository ingestionStrategyEntityRepository;

  @Override
  public IngestionStrategy findByWorkflowTypeAndDataTypeAndUserId(WorkflowType workflowType,
      String dataType, String userId) {
    log.debug("Requesting dag selection. Workflow type : {}, Data type : {}, User id : {}",
        workflowType, dataType, userId);
    IngestionStrategyEntity entity = ingestionStrategyEntityRepository
        .findByWorkflowTypeAndDataTypeAndUserId(asString(workflowType), dataType,
            userId);
    IngestionStrategy ingestionStrategy = ingestionStrategyMapper.toIngestionStrategy(entity);
    log.debug("Found dag : {}", ingestionStrategy);
    return ingestionStrategy;
  }

  private String asString(Enum<?> e) {
    if (e == null) {
      return null;
    }

    return e.name().toLowerCase();
  }

}
