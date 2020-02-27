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

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.gcp.mapper.EnumMapper;
import org.opengroup.osdu.workflow.provider.gcp.mapper.IngestionStrategyMapper;
import org.opengroup.osdu.workflow.provider.gcp.model.IngestionStrategyEntity;
import org.opengroup.osdu.workflow.provider.interfaces.IngestionStrategyRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class DatastoreIngestionStrategyRepositoryTest {

  private static final String INGEST_WORKFLOW_TYPE = "ingest";
  private static final String WELL_LOG_DATA_TYPE = "well_log";
  private static final String USER = "user-1";

  @Spy
  private IngestionStrategyMapper ingestionStrategyMapper = Mappers.getMapper(IngestionStrategyMapper.class);
  @Mock
  private IngestionStrategyEntityRepository ingestionStrategyEntityRepository;

  private IngestionStrategyRepository ingestionStrategyRepository;

  @BeforeEach
  void setUp() {
    EnumMapper enumMapper = new EnumMapper();
    ReflectionTestUtils.setField(ingestionStrategyMapper, "enumMapper", enumMapper);

    ingestionStrategyRepository = new DatastoreIngestionStrategyRepository(ingestionStrategyMapper,
        ingestionStrategyEntityRepository);
  }

  @Test
  void shouldFindIngestionStrategyByWorkflowTypeAndDataType() {
    // given
    given(ingestionStrategyEntityRepository.findByWorkflowTypeAndDataTypeAndUserId(
        INGEST_WORKFLOW_TYPE, WELL_LOG_DATA_TYPE, USER))
        .willReturn(getIngestionStrategyEntity());

    // when
    IngestionStrategy ingestionStrategy = ingestionStrategyRepository
        .findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, DataType.WELL_LOG, USER);

    // then
    then(ingestionStrategy).isEqualTo(getIngestionStrategy());

    InOrder inOrder = Mockito.inOrder(ingestionStrategyEntityRepository, ingestionStrategyMapper);
    inOrder.verify(ingestionStrategyEntityRepository)
        .findByWorkflowTypeAndDataTypeAndUserId(INGEST_WORKFLOW_TYPE, WELL_LOG_DATA_TYPE, USER);
    inOrder.verify(ingestionStrategyMapper).toIngestionStrategy(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldReturnNullWhenNothingWasFound() {
    // when
    IngestionStrategy ingestionStrategy = ingestionStrategyRepository
        .findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, DataType.WELL_LOG, USER);

    // then
    then(ingestionStrategy).isNull();

    InOrder inOrder = Mockito.inOrder(ingestionStrategyEntityRepository, ingestionStrategyMapper);
    inOrder.verify(ingestionStrategyEntityRepository)
        .findByWorkflowTypeAndDataTypeAndUserId(INGEST_WORKFLOW_TYPE, WELL_LOG_DATA_TYPE, USER);
    inOrder.verify(ingestionStrategyMapper).toIngestionStrategy(any());
    inOrder.verifyNoMoreInteractions();
  }

  private IngestionStrategyEntity getIngestionStrategyEntity() {
    return IngestionStrategyEntity.builder()
        .workflowType(WorkflowType.INGEST.name())
        .dataType(DataType.WELL_LOG.name())
        .userId(USER)
        .build();
  }

  private IngestionStrategy getIngestionStrategy() {
    return IngestionStrategy.builder()
        .workflowType(WorkflowType.INGEST)
        .dataType(DataType.WELL_LOG)
        .userId(USER)
        .build();
  }

}
