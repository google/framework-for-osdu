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

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.exception.IngestionStrategyNotFoundException;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.repository.IngestionStrategyRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class IngestionStrategyServiceTest {

  private static final String USER_1 = "User1";
  private static final String USER_2 = "User2";
  private static final String DEFAULT_INGEST_PY = "Default_ingest.py";
  private static final String WELL_LOG_INGEST_PY = "Well_log_ingest.py";
  private static final String OTHER_LOG_INGEST_PY = "Other_log_ingest.py";

  @Mock
  private IngestionStrategyRepository ingestionStrategyRepository;

  IngestionStrategyService ingestionStrategyService;

  @BeforeEach
  void setUp() {
    ingestionStrategyService = new IngestionStrategyServiceImpl(ingestionStrategyRepository);
    // default repo answer
    given(ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(any(),
        any(), any())).willReturn(null);
  }

  @Test
  void shouldUseOpaqueDataTypeAsDefault() {

    // given
    given(ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(eq(WorkflowType.INGEST),
        isNull(), isNull()))
        .willReturn(IngestionStrategy.builder().dagName(DEFAULT_INGEST_PY).build());

    // when
    String dagName = ingestionStrategyService
        .determineStrategy(WorkflowType.INGEST, DataType.OPAQUE, USER_1);

    // then
    then(dagName).isEqualTo(DEFAULT_INGEST_PY);
  }

  @Test
  void shouldDetermineDAgSpecifiedForUser() {

    // given
    given(ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(eq(WorkflowType.INGEST),
        eq(DataType.WELL_LOG), eq(USER_1)))
        .willReturn(IngestionStrategy.builder().dagName(OTHER_LOG_INGEST_PY).build());

    // when
    String dagName = ingestionStrategyService
        .determineStrategy(WorkflowType.INGEST, DataType.WELL_LOG, USER_1);

    // then
    then(dagName).isEqualTo(OTHER_LOG_INGEST_PY);
  }

  @Test
  void shouldUseCommonDagIfUserIsNotMatched() {

    // given
    given(ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(eq(WorkflowType.INGEST),
        eq(DataType.WELL_LOG), isNull()))
        .willReturn(IngestionStrategy.builder().dagName(WELL_LOG_INGEST_PY).build());

    // when
    String dagName = ingestionStrategyService
        .determineStrategy(WorkflowType.INGEST, DataType.WELL_LOG, USER_2);

    // then
    then(dagName).isEqualTo(WELL_LOG_INGEST_PY);
  }

  @Test
  void shouldThrowExceptionIfDagNotFound() {

    // when
    Throwable thrown = catchThrowable(() -> ingestionStrategyService
        .determineStrategy(WorkflowType.INGEST, DataType.WELL_LOG, null));

    // then
    then(thrown).isInstanceOf(IngestionStrategyNotFoundException.class);
  }

}
