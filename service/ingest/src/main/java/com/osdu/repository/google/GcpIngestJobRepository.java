/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.repository.google;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.osdu.exception.IngestJobException;
import com.osdu.exception.SrnMappingException;
import com.osdu.model.job.IngestJob;
import com.osdu.repository.IngestJobRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class GcpIngestJobRepository implements IngestJobRepository {

  private static final String COLLECTION_NAME = "ingestJob";
  private static final String ID_FIELD_NAME = "id";

  final Firestore firestore;

  public GcpIngestJobRepository() {
    this.firestore = FirestoreOptions.getDefaultInstance().getService();
  }

  @Override
  public IngestJob findById(String id) {
    log.debug("Requesting ingest job id : {}", id);

    final ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereEqualTo(ID_FIELD_NAME, id).get();

    final QuerySnapshot querySnapshot;
    try {
      querySnapshot = query.get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new SrnMappingException(String.format("Failed to Ingest job for id %s", id), e);
    }
    final List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new IngestJobException(String
          .format("Find by Id returned %s document(s), expected 1, query id : %s",
              documents.size(), id));
    }

    IngestJob job =
        documents.isEmpty() ? null : documents.get(0).toObject(IngestJob.class);
    log.debug("Ingest job request resulted with : {}", job);

    return job;
  }

  @Override
  public void save(IngestJob ingestJob) {
    log.debug("Request to save ingest job : {}", ingestJob);
    try {
      final WriteResult writeResult = firestore.collection(COLLECTION_NAME)
          .document(ingestJob.getId())
          .set(ingestJob, SetOptions.merge()).get();
      log.debug("Ingest job : {} saved on : {}", ingestJob, writeResult.getUpdateTime());
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new IngestJobException(
          String.format("Exception during saving of ingest job : %s", ingestJob), e);
    }
  }

  @Override
  public void updateFields(String id, Map<String, Object> fields) {
    log.debug("Request to update ingest job. Id: {}, fields: {}", id, fields);
    try {
      WriteResult writeResult = firestore.collection(COLLECTION_NAME)
          .document(id)
          .update(fields).get();
      log.debug("Ingest job is updated. Id = {}, saved on: {}", id, writeResult.getUpdateTime());
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new IngestJobException(
          String.format("Exception during updating of ingest job. Id: %s, fields: %s", id, fields),
          e);
    }
  }
}
