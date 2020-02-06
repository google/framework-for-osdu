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
import com.google.cloud.firestore.WriteResult;
import com.osdu.exception.SrnMappingException;
import com.osdu.model.dto.SrnToRecordDto;
import com.osdu.repository.SrnToRecordRepository;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class GcpSrnToRecordRepository implements SrnToRecordRepository {

  private static final String COLLECTION_NAME = "recordBySrn";
  private static final Pattern DOCUMENT_ID_PATTERN = Pattern.compile("[/]+");
  private static final String SRN_FIELD_NAME = "srn";

  final Firestore firestore;

  public GcpSrnToRecordRepository() {
    this.firestore = FirestoreOptions.getDefaultInstance().getService();
  }

  @Override
  public SrnToRecordDto findBySrn(String srn) {
    log.debug("Requesting record by srn: {}", srn);

    ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereEqualTo(SRN_FIELD_NAME, srn).get();
    QuerySnapshot querySnapshot;
    try {
      querySnapshot = query.get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new SrnMappingException("Failed to SrnToRecord for srn: " + srn, e);
    }

    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new SrnMappingException(String
          .format("Find by srn returned %s document(s), expected 1, query srn: %s",
              documents.size(), srn));
    }

    SrnToRecordDto record = documents.isEmpty() ? null
        : documents.get(0).toObject(SrnToRecordDto.class);

    log.debug("Srn request resulted in record: {}", record);
    return record;
  }

  @Override
  public void save(SrnToRecordDto record) {
    String normalizedSrn = getNormalizedSrn(record.getSrn());
    log.debug("Request to save srnToRecord: {}, id as normalized srn: {}", record, normalizedSrn);

    try {
      WriteResult writeResult = firestore.collection(COLLECTION_NAME)
          .document(normalizedSrn)
          .set(record).get();
      log.debug("SrnToRecord: {} saved on: {}", record, writeResult.getUpdateTime());
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new SrnMappingException("Exception during saving of srnToRecord: " + record, e);
    }
  }

  private String getNormalizedSrn(String srn) {
    return RegExUtils.replaceAll(srn, DOCUMENT_ID_PATTERN, "");
  }

}
