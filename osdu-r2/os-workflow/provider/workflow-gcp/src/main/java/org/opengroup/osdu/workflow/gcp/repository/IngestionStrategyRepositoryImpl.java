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

package org.opengroup.osdu.workflow.gcp.repository;

import static java.lang.String.format;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.DAG_NAME;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.DATA_TYPE;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.USER_ID;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.WORKFLOW_TYPE;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.gcp.exception.IngestionStrategyQueryException;
import org.opengroup.osdu.workflow.gcp.property.DatabaseProperties;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.repository.IngestionStrategyRepository;
import org.springframework.stereotype.Repository;

// TODO Will be moved to registry service
@Repository
@Slf4j
@RequiredArgsConstructor
public class IngestionStrategyRepositoryImpl implements IngestionStrategyRepository {

  final DatabaseProperties databaseProperties;

  final Firestore firestore;

  @Override
  public IngestionStrategy findByWorkflowTypeAndDataTypeAndUserId(WorkflowType workflowType,
      DataType dataType, String userId) {
    log.debug("Requesting dag selection. Workflow type : {}, Data type : {}, User id : {}",
        workflowType, dataType, userId);
    String collectionName = databaseProperties.getIngestionStrategy();
    ApiFuture<QuerySnapshot> query = firestore.collection(collectionName)
        .whereEqualTo(WORKFLOW_TYPE, workflowType)
        .whereEqualTo(DATA_TYPE, dataType)
        .whereEqualTo(USER_ID, userId)
        .get();

    QuerySnapshot querySnapshot = getSafety(query,
        format("Failed to find a dag by Workflow type - %s, Data type - %s and User id - %s",
            workflowType, dataType, userId));

    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new IngestionStrategyQueryException(
          format(
              "Find dag selection returned %s documents(s), expected 1, query by Workflow type - %s, Data type - %s and User id - %s",
              documents.size(), workflowType, dataType, userId));
    }

    IngestionStrategy ingestionStrategy = documents.isEmpty()
        ? null
        : buildIngestionStrategy(documents.get(0));

    log.debug("Found dag : {}", ingestionStrategy);
    return ingestionStrategy;
  }

  private <T> T getSafety(Future<T> future, String errorMsg) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IngestionStrategyQueryException(errorMsg, e);
    } catch (ExecutionException e) {
      throw new IngestionStrategyQueryException(errorMsg, e);
    }
  }

  private IngestionStrategy buildIngestionStrategy(DocumentSnapshot snap) {
    log.info("Build ingestion strategy. Document snapshot : {}", snap.getData());
    return IngestionStrategy.builder()
        .workflowType(WorkflowType.valueOf(snap.getString(WORKFLOW_TYPE)))
        .dataType(snap.getString(DATA_TYPE) == null ? null :
            DataType.valueOf(snap.getString(DATA_TYPE)))
        .userId(snap.getString(USER_ID))
        .dagName(snap.getString(DAG_NAME))
        .build();
  }
}
