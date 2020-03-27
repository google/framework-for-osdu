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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.AIRFLOW_RUN_ID;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.SUBMITTED_AT;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.SUBMITTED_BY;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.WORKFLOW_ID;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.WORKFLOW_STATUS_TYPE;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusNotFoundException;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusNotUpdatedException;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusQueryException;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class FirestoreWorkflowStatusRepositoryTest {

  private static final String COLLECTION_NAME = "workflow-status";
  private static final String TEST_WORKFLOW_ID = "test-workflow-id";
  private static final String TEST_AIRFLOW_RUN_ID = "test-airflow-run-id";
  private static final String USER = "user-1";

  private QueryDocumentSnapshot qDocSnap = mock(QueryDocumentSnapshot.class);
  private DocumentReference docRef = mock(DocumentReference.class);
  private DocumentSnapshot docSnap = mock(DocumentSnapshot.class);
  private Firestore firestore = mock(Firestore.class, RETURNS_DEEP_STUBS);
  private WriteResult writeResult = mock(WriteResult.class, RETURNS_DEEP_STUBS);

  private IWorkflowStatusRepository workflowStatusRepository;

  @BeforeEach
  void setUp() {
    workflowStatusRepository = new FirestoreWorkflowStatusRepository(firestore);
  }

  @Nested
  class FindWorkflowStatus {

    @Test
    void shouldFindWorkflowStatusByWorkflowId() {

      // given
      Date createdDate = new Date();
      List<QueryDocumentSnapshot> documents = Collections.singletonList(qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .willReturn(queryFuture);

      givenDocSnap(qDocSnap, getWorkflowStatus(createdDate));

      // when
      WorkflowStatus workflowStatus = workflowStatusRepository
          .findWorkflowStatus(TEST_WORKFLOW_ID);

      // then
      then(workflowStatus).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenQueryFailed() {

      // given
      ApiFuture<QuerySnapshot> queryFuture =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed query"));

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository.findWorkflowStatus(
          TEST_WORKFLOW_ID));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusQueryException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasMessage("Failed to find a workflow status by Workflow id - test-workflow-id");
    }

    @Test
    void shouldThrowExceptionWhenFutureFailed() throws Exception {

      // given
      ApiFuture queryFuture = mock(ApiFuture.class);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .willReturn(queryFuture);
      willThrow(new InterruptedException("Failed future")).given(queryFuture).get();

      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository.findWorkflowStatus(
          TEST_WORKFLOW_ID));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusQueryException.class)
          .hasRootCauseInstanceOf(InterruptedException.class)
          .hasMessage("Failed to find a workflow status by Workflow id - test-workflow-id");
    }

    @Test
    void shouldThrowExceptionWhenItFindsFewDocuments() {
      // given
      List<QueryDocumentSnapshot> documents = Arrays.asList(qDocSnap, qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository.findWorkflowStatus(
          TEST_WORKFLOW_ID));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusQueryException.class)
          .hasMessage(
              "Find workflow status returned 2 documents(s), expected 1, query by Workflow id - test-workflow-id");
    }

    @Test
    void shouldReturnNullWhenNothingWasFound() {
      // given
      List<QueryDocumentSnapshot> documents = Collections.emptyList();
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, "test").get())
          .willReturn(queryFuture);

      // when
      WorkflowStatus workflowStatus = workflowStatusRepository.findWorkflowStatus("test");

      // then
      then(workflowStatus).isNull();
    }

  }

  @Nested
  class SaveWorkflowStatus {

    @Captor
    ArgumentCaptor<Map<String, Object>> dataCaptor;

    @Test
    void shouldSaveWorkflowStatusAndReturnSavedEntity() {
      // given
      Date createdDate = new Date();
      WorkflowStatus workflowStatus = getWorkflowStatus(createdDate);

      ApiFuture<DocumentReference> query = ApiFutures.immediateFuture(docRef);
      ApiFuture<DocumentSnapshot> savedDoc = ApiFutures.immediateFuture(docSnap);

      given(firestore.collection(COLLECTION_NAME).add(anyMap())).willReturn(query);
      given(docRef.get()).willReturn(savedDoc);

      given(docSnap.getString(WorkflowStatus.Fields.WORKFLOW_ID)).willReturn(TEST_WORKFLOW_ID);
      given(docSnap.getString(AIRFLOW_RUN_ID)).willReturn(
          TEST_AIRFLOW_RUN_ID);
      given(docSnap.getString(WORKFLOW_STATUS_TYPE))
          .willReturn(WorkflowStatusType.SUBMITTED.name());
      given(docSnap.getDate(SUBMITTED_AT)).willReturn(createdDate);
      given(docSnap.getString(SUBMITTED_BY)).willReturn(USER);

      // when
      WorkflowStatus saved = workflowStatusRepository.saveWorkflowStatus(workflowStatus);

      // then
      then(saved).isEqualTo(workflowStatus);
    }

    @Test
    void shouldUseServerTimestampWhenCreateAtIsNotSpecified() {
      // given
      WorkflowStatus workflowStatus = WorkflowStatus.builder()
          .workflowId(TEST_WORKFLOW_ID)
          .airflowRunId(TEST_AIRFLOW_RUN_ID)
          .workflowStatusType(WorkflowStatusType.SUBMITTED)
          .submittedBy(USER)
          .build();

      ApiFuture<DocumentReference> query = ApiFutures.immediateFuture(docRef);
      ApiFuture<DocumentSnapshot> savedDoc = ApiFutures.immediateFuture(docSnap);

      given(firestore.collection(COLLECTION_NAME).add(anyMap())).willReturn(query);
      given(docRef.get()).willReturn(savedDoc);

      givenDocSnap(docSnap, workflowStatus);

      // when
      WorkflowStatus saved = workflowStatusRepository.saveWorkflowStatus(workflowStatus);

      // then
      then(saved).isEqualToIgnoringGivenFields(saved, SUBMITTED_AT);

      verify(firestore.collection(COLLECTION_NAME)).add(dataCaptor.capture());

      then(dataCaptor.getValue()).satisfies(map -> {
        then(map.get(WORKFLOW_ID)).isEqualTo(TEST_WORKFLOW_ID);
        then(map.get(SUBMITTED_AT)).isEqualTo(FieldValue.serverTimestamp());
      });
    }

    @Test
    void shouldThrowExceptionWhenSaveQueryFailed() {
      // given
      Date createdDate = new Date();
      WorkflowStatus workflowStatus = getWorkflowStatus(createdDate);

      ApiFuture<DocumentReference> query =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed query"));

      given(firestore.collection(COLLECTION_NAME).add(anyMap())).willReturn(query);

      // when
      Throwable thrown = catchThrowable(
          () -> workflowStatusRepository.saveWorkflowStatus(workflowStatus));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusQueryException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Exceptions during saving  workflow status:");
    }

    @Test
    void shouldThrowExceptionWhenUnableToFetchSavedEntity() {
      // given
      Date createdDate = new Date();
      WorkflowStatus workflowStatus = getWorkflowStatus(createdDate);

      ApiFuture<DocumentReference> query = ApiFutures.immediateFuture(docRef);
      ApiFuture<DocumentSnapshot> savedDoc =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed get saved"));

      given(firestore.collection(COLLECTION_NAME).add(anyMap())).willReturn(query);
      given(docRef.get()).willReturn(savedDoc);

      // when
      Throwable thrown = catchThrowable(
          () -> workflowStatusRepository.saveWorkflowStatus(workflowStatus));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusQueryException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasMessage("Saved Workflow status should exist");
    }
  }

  @Nested
  class UpdateWorkflowStatus {

    @Test
    void shouldUpdateWorkflowStatusAndReturnSavedEntity() {

      // given
      Date createdDate = new Date();
      List<QueryDocumentSnapshot> documents = Collections.singletonList(qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      lenient().when(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .thenReturn(queryFuture);

      givenDocSnap(qDocSnap, getWorkflowStatus(createdDate));

      ApiFuture<WriteResult> queryWriteFuture = ApiFutures.immediateFuture(writeResult);

      lenient().when(firestore.collection(COLLECTION_NAME).document(eq("doc-id"))
          .update(eq(WORKFLOW_STATUS_TYPE), eq("running"))).thenReturn(queryWriteFuture);

      // when
      WorkflowStatus saved = workflowStatusRepository
          .updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.RUNNING);

      // then
      then(saved.getWorkflowStatusType()).isEqualTo(WorkflowStatusType.RUNNING);
      then(saved.getWorkflowId()).isEqualTo(TEST_WORKFLOW_ID);
    }

    @Test
    void shouldThrowExceptionWhenUpdateQueryFailed() {

      // given
      ApiFuture<QuerySnapshot> queryFuture =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed query"));

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository.updateWorkflowStatus(
          TEST_WORKFLOW_ID, WorkflowStatusType.RUNNING));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusQueryException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasMessage("Failed to find a workflow status by Workflow id - test-workflow-id");
    }

    @Test
    void shouldThrowExceptionWhenItFindsFewDocuments() {

      // given
      List<QueryDocumentSnapshot> documents = Arrays.asList(qDocSnap, qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository.updateWorkflowStatus(
          TEST_WORKFLOW_ID, WorkflowStatusType.RUNNING));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusQueryException.class)
          .hasMessage(
              "Found more than one (2) workflow status documents, expected 1, query by Workflow id - test-workflow-id");
    }

    @Test
    void shouldThrowExceptionWhenNothingWasFound() {
      // given
      List<QueryDocumentSnapshot> documents = Collections.emptyList();
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository
          .updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.RUNNING));

      // then
      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusNotFoundException.class)
          .hasMessage("Workflow status for Workflow id: test-workflow-id not found");
    }

    @Test
    void shouldThrowExceptionIfWorkflowHasAlreadyDefinedStatus() {

      // given
      Date createdDate = new Date();
      List<QueryDocumentSnapshot> documents = Collections.singletonList(qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      lenient().when(firestore.collection(COLLECTION_NAME)
          .whereEqualTo(WORKFLOW_ID, TEST_WORKFLOW_ID).get())
          .thenReturn(queryFuture);

      givenDocSnap(qDocSnap, getWorkflowStatus(createdDate));

      // when
      Throwable thrown = catchThrowable(() -> workflowStatusRepository.updateWorkflowStatus(
          TEST_WORKFLOW_ID, WorkflowStatusType.SUBMITTED));

      // then
      then(thrown)
          .isInstanceOf(WorkflowStatusNotUpdatedException.class)
          .hasMessage(
              "Workflow status for workflow id: test-workflow-id already has status:SUBMITTED and can not be updated");
    }
  }

  private WorkflowStatus getWorkflowStatus(Date createdDate) {
    return WorkflowStatus.builder()
        .workflowId(TEST_WORKFLOW_ID)
        .airflowRunId(TEST_AIRFLOW_RUN_ID)
        .workflowStatusType(WorkflowStatusType.SUBMITTED)
        .submittedAt(createdDate)
        .submittedBy(USER)
        .build();
  }

  private void givenDocSnap(DocumentSnapshot qDocSnap, WorkflowStatus workflowStatus) {
    given(qDocSnap.getString(WORKFLOW_ID)).willReturn(workflowStatus.getWorkflowId());
    given(qDocSnap.getString(AIRFLOW_RUN_ID)).willReturn(workflowStatus.getAirflowRunId());
    given(qDocSnap.getString(WORKFLOW_STATUS_TYPE))
        .willReturn(workflowStatus.getWorkflowStatusType().name());
    given(qDocSnap.getDate(SUBMITTED_AT)).willReturn(workflowStatus.getSubmittedAt());
    given(qDocSnap.getString(SUBMITTED_BY)).willReturn(workflowStatus.getSubmittedBy());
  }

}
