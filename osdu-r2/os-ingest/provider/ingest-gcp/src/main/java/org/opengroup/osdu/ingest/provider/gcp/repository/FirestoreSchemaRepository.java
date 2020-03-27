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

package org.opengroup.osdu.ingest.provider.gcp.repository;

import static java.lang.String.format;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.ingest.exception.SchemaDataQueryException;
import org.opengroup.osdu.ingest.model.SchemaData;
import org.opengroup.osdu.ingest.model.SchemaData.Fields;
import org.opengroup.osdu.ingest.provider.gcp.mapper.ISchemaDataMapper;
import org.opengroup.osdu.ingest.provider.gcp.model.dto.SchemaDataDto;
import org.opengroup.osdu.ingest.provider.interfaces.ISchemaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FirestoreSchemaRepository implements ISchemaRepository {

  private static final String COLLECTION_NAME = "schema-data";

  final Firestore firestore;
  @Named
  final ISchemaDataMapper schemaDataMapper;

  @Override
  public SchemaData findByTitle(String title) {
    log.debug("Requesting schema data. Schema title : {}", title);
    ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereEqualTo(Fields.TITLE, title).get();

    QuerySnapshot querySnapshot = getSafety(query,
        "Failed to find a schema data by title " + title);

    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new SchemaDataQueryException(
          format("Find by ID returned %s documents(s), expected 1, query ID : %s",
              documents.size(), title));
    }

    SchemaData schemaData = documents.isEmpty()
        ? null
        : buildSchemaData(documents.get(0));

    log.debug("Found schema data : {}", schemaData);
    return schemaData;
  }

  private <T> T getSafety(Future<T> future, String errorMsg) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SchemaDataQueryException(errorMsg, e);
    } catch (ExecutionException e) {
      throw new SchemaDataQueryException(errorMsg, e);
    }
  }

  private SchemaData buildSchemaData(QueryDocumentSnapshot snapshot) {
    SchemaDataDto dto = SchemaDataDto.builder()
        .title(snapshot.getString(Fields.TITLE))
        .schema(snapshot.getString(Fields.SCHEMA))
        .createdAt(snapshot.getDate(Fields.CREATED_AT))
        .build();
    return schemaDataMapper.schemaDataDtoToSchemaData(dto);
  }

}
