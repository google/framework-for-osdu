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

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.DAG_NAME;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.DATA_TYPE;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.USER_ID;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.WORKFLOW_TYPE;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.gcp.exception.IngestionStrategyQueryException;
import org.opengroup.osdu.workflow.provider.interfaces.IngestionStrategyRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class FirestoreIngestionStrategyRepositoryTest {

  private static final String COLLECTION_NAME = "ingestion-strategy";
  private static final String USER = "user-1";

  private QueryDocumentSnapshot qDocSnap = mock(QueryDocumentSnapshot.class);
  private Firestore firestore = mock(Firestore.class, RETURNS_DEEP_STUBS);

  private IngestionStrategyRepository ingestionStrategyRepository;

  @BeforeEach
  void setUp() {
    ingestionStrategyRepository = new FirestoreIngestionStrategyRepository(firestore);
  }

  @Nested
  class FindIngestionStrategy {

    @Test
    void shouldFindIngestionStrategyByWorkflowId() {
      // given
      List<QueryDocumentSnapshot> documents = Collections.singletonList(qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_TYPE, WorkflowType.INGEST)
          .whereEqualTo(DATA_TYPE, "well_log")
          .whereEqualTo(USER_ID, USER)
          .get())
          .willReturn(queryFuture);

      givenDocSnap(qDocSnap, getIngestionStrategy());

      // when
      IngestionStrategy IngestionStrategy = ingestionStrategyRepository
          .findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, "well_log", USER);

      // then
      then(IngestionStrategy).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenQueryFailed() {
      // given
      ApiFuture<QuerySnapshot> queryFuture =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed query"));

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_TYPE, WorkflowType.INGEST)
          .whereEqualTo(DATA_TYPE, "well_log")
          .whereEqualTo(USER_ID, USER).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(
          () -> ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(
              WorkflowType.INGEST, "well_log", USER));

      // then
      then(thrown)
          .isInstanceOf(IngestionStrategyQueryException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasMessage(
              "Failed to find a dag by Workflow type - INGEST, Data type - well_log and User id - user-1");
    }

    @Test
    void shouldThrowExceptionWhenFutureFailed() throws Exception {
      // given
      ApiFuture queryFuture = mock(ApiFuture.class);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_TYPE, WorkflowType.INGEST)
          .whereEqualTo(DATA_TYPE, "well_log")
          .whereEqualTo(USER_ID, USER).get())
          .willReturn(queryFuture);
      willThrow(new InterruptedException("Failed future")).given(queryFuture).get();

      // when
      Throwable thrown = catchThrowable(
          () -> ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(
              WorkflowType.INGEST, "well_log", USER));

      // then
      then(thrown)
          .isInstanceOf(IngestionStrategyQueryException.class)
          .hasRootCauseInstanceOf(InterruptedException.class)
          .hasMessage(
              "Failed to find a dag by Workflow type - INGEST, Data type - well_log and User id - user-1");
    }

    @Test
    void shouldThrowExceptionWhenItFindsFewDocuments() {
      // given
      List<QueryDocumentSnapshot> documents = Arrays.asList(qDocSnap, qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_TYPE, WorkflowType.INGEST)
          .whereEqualTo(DATA_TYPE, "well_log")
          .whereEqualTo(USER_ID, USER).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(
          () -> ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(
              WorkflowType.INGEST, "well_log", USER));

      // then
      then(thrown)
          .isInstanceOf(IngestionStrategyQueryException.class)
          .hasMessage(
              "Find dag selection returned 2 documents(s), expected 1, query by Workflow type - INGEST, Data type - well_log and User id - user-1");
    }

    @Test
    void shouldReturnNullWhenNothingWasFound() {
      // given
      List<QueryDocumentSnapshot> documents = Collections.emptyList();
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_TYPE, WorkflowType.INGEST)
          .whereEqualTo(DATA_TYPE, "well_log")
          .whereEqualTo(USER_ID, USER).get())
          .willReturn(queryFuture);

      // when
      IngestionStrategy IngestionStrategy = ingestionStrategyRepository
          .findByWorkflowTypeAndDataTypeAndUserId(
              WorkflowType.INGEST, "well_log", USER);

      // then
      then(IngestionStrategy).isNull();
    }

  }

  private IngestionStrategy getIngestionStrategy() {
    return IngestionStrategy.builder()
        .workflowType(WorkflowType.INGEST)
        .dataType("well_log")
        .userId(USER)
        .build();
  }

  private void givenDocSnap(DocumentSnapshot qDocSnap, IngestionStrategy strategy) {
    given(qDocSnap.getString(WORKFLOW_TYPE)).willReturn(strategy.getWorkflowType().name());
    given(qDocSnap.getString(DATA_TYPE)).willReturn(strategy.getDataType());
    given(qDocSnap.getString(USER_ID)).willReturn(strategy.getUserId());
    given(qDocSnap.getString(DAG_NAME)).willReturn(strategy.getDagName());
  }

}
