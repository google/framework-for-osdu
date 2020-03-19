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

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.exception.IngestionStrategyNotFoundException;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.property.DataTypeProperties;
import org.opengroup.osdu.workflow.provider.interfaces.IngestionStrategyRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IngestionStrategyService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngestionStrategyServiceImpl implements IngestionStrategyService {

  final IngestionStrategyRepository ingestionStrategyRepository;
  final DataTypeProperties dataTypeProperties;

  @Override
  public String determineStrategy(WorkflowType workflowType, String dataType, String userId) {

    String defaultType = dataTypeProperties.getDefaultType();

    IngestionStrategy ingestionStrategy = ingestionStrategyRepository
        .findByWorkflowTypeAndDataTypeAndUserId(workflowType, dataType, userId);

    if (ingestionStrategy == null && defaultType.equals(dataType)) {
      ingestionStrategy = ingestionStrategyRepository
          .findByWorkflowTypeAndDataTypeAndUserId(workflowType, null, userId);
    }

    if (ingestionStrategy == null && userId != null) {
      ingestionStrategy = ingestionStrategyRepository
          .findByWorkflowTypeAndDataTypeAndUserId(workflowType, dataType, null);
    }

    if (ingestionStrategy == null && userId != null && defaultType.equals(dataType)) {
      ingestionStrategy = ingestionStrategyRepository
          .findByWorkflowTypeAndDataTypeAndUserId(workflowType, null, null);
    }

    if (ingestionStrategy == null) {
      throw new IngestionStrategyNotFoundException(String
          .format("Dag for Workflow type - %s, Data type - %s and User id - %s not found",
              workflowType, dataType, userId));
    }

    return ingestionStrategy.getDagName();
  }

}
