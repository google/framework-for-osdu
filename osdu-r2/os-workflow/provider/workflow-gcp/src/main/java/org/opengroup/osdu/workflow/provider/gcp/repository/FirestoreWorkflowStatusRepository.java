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

import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.AIRFLOW_RUN_ID;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.SUBMITTED_AT;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.SUBMITTED_BY;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.WORKFLOW_ID;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.WORKFLOW_STATUS_TYPE;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusNotFoundException;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusNotUpdatedException;
import org.opengroup.osdu.workflow.provider.gcp.exception.WorkflowStatusQueryException;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FirestoreWorkflowStatusRepository implements IWorkflowStatusRepository {

  private static final String COLLECTION_NAME = "workflow-status";

  final Firestore firestore;

  @Override
  public WorkflowStatus findWorkflowStatus(String workflowId) {
    log.debug("Requesting workflow status by workflow id - {}", workflowId);
    ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereEqualTo(WORKFLOW_ID, workflowId)
        .get();

    QuerySnapshot querySnapshot = getSafety(query,
        String.format("Failed to find a workflow status by Workflow id - %s", workflowId));

    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new WorkflowStatusQueryException(
          String.format("Find workflow status returned %s documents(s), expected 1, query by"
                  + " Workflow id - %s",
              documents.size(), workflowId));
    }

    WorkflowStatus workflowStatus = documents.isEmpty()
        ? null
        : buildWorkflowStatus(documents.get(0));

    log.debug("Found workflow status : {}", workflowStatus);
    return workflowStatus;
  }

  @Override
  public WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus) {

    log.info("Saving workflow status  location : {}", workflowStatus);
    final String errorMsg = "Exceptions during saving  workflow status: " + workflowStatus;

    Map<String, Object> data = getWorkflowStatusData(workflowStatus);
    ApiFuture<DocumentReference> query = firestore.collection(COLLECTION_NAME).add(data);
    DocumentReference addedDocRef = getSafety(query, errorMsg);
    log.info("Fetch DocumentReference pointing to a new document with an auto-generated ID : {}",
        addedDocRef);

    DocumentSnapshot saved = getSafety(addedDocRef.get(),
        "Saved Workflow status should exist");
    log.info("Fetch saved workflow status : {}", saved);
    return buildWorkflowStatus(saved);
  }

  @Override
  public WorkflowStatus updateWorkflowStatus(String workflowId,
      WorkflowStatusType workflowStatusType) {

    log.info("Update workflow status  for workflow id: {}, new status: {}", workflowId,
        workflowStatusType);

    ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereEqualTo(WORKFLOW_ID, workflowId)
        .get();

    QuerySnapshot querySnapshot = getSafety(query,
        String.format("Failed to find a workflow status by Workflow id - %s", workflowId));

    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new WorkflowStatusQueryException(
          String.format("Found more than one (%s) workflow status documents, expected 1, query by"
                  + " Workflow id - %s",
              documents.size(), workflowId));
    }

    if (documents.isEmpty()) {
      throw new WorkflowStatusNotFoundException(
          String.format("Workflow status for Workflow id: %s not found", workflowId));
    }

    String documentId = documents.get(0).getId();
    WorkflowStatus workflowStatus = buildWorkflowStatus(documents.get(0));

    if (workflowStatus.getWorkflowStatusType().equals(workflowStatusType)) {
      throw new WorkflowStatusNotUpdatedException(String.format(
          "Workflow status for workflow id: %s already has status:%s and can not be updated",
          workflowId, workflowStatusType));
    }

    workflowStatus.setWorkflowStatusType(workflowStatusType);

    ApiFuture<WriteResult> updateQuery = firestore.collection(COLLECTION_NAME).document(documentId)
        .update(WORKFLOW_STATUS_TYPE, workflowStatusType.toString());

    getSafety(updateQuery,
        String.format("Failed to update workflow status document, workflow id: %s", workflowId));

    return workflowStatus;
  }

  private <T> T getSafety(Future<T> future, String errorMsg) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new WorkflowStatusQueryException(errorMsg, e);
    } catch (ExecutionException e) {
      throw new WorkflowStatusQueryException(errorMsg, e);
    }
  }

  private WorkflowStatus buildWorkflowStatus(DocumentSnapshot snap) {
    log.info("Build workflow status. Document snapshot : {}", snap.getData());
    return WorkflowStatus.builder()
        .workflowId(snap.getString(WORKFLOW_ID))
        .airflowRunId(snap.getString(AIRFLOW_RUN_ID))
        .workflowStatusType(WorkflowStatusType.valueOf(snap.getString(WORKFLOW_STATUS_TYPE)))
        .submittedAt(snap.getDate(SUBMITTED_AT))
        .submittedBy(snap.getString(SUBMITTED_BY)).build();
  }

  private Map<String, Object> getWorkflowStatusData(WorkflowStatus workflowStatus) {
    Object submittedAt = ObjectUtils.defaultIfNull(workflowStatus.getSubmittedAt(),
        FieldValue.serverTimestamp());

    Map<String, Object> data = new HashMap<>();
    data.put(WORKFLOW_ID, workflowStatus.getWorkflowId());
    data.put(AIRFLOW_RUN_ID, workflowStatus.getAirflowRunId());
    data.put(WORKFLOW_STATUS_TYPE, workflowStatus.getWorkflowStatusType().name());
    data.put(SUBMITTED_AT, submittedAt);
    data.put(SUBMITTED_BY, workflowStatus.getSubmittedBy());
    return data;
  }
}
