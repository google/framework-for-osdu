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

package org.opengroup.osdu.ingest.provider.gcp.repository;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.opengroup.osdu.ingest.ResourceUtils.getResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.exception.SchemaDataQueryException;
import org.opengroup.osdu.ingest.model.SchemaData;
import org.opengroup.osdu.ingest.model.SchemaData.Fields;
import org.opengroup.osdu.ingest.provider.gcp.mapper.SchemaDataMapper;
import org.opengroup.osdu.ingest.provider.gcp.model.dto.SchemaDataDto;
import org.opengroup.osdu.ingest.provider.interfaces.SchemaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class FirestoreSchemaRepositoryTest {

  private static final String COLLECTION_NAME = "schema-data";
  private static final String SCHEMA_TITLE = "test-schema-title";
  private static final String DRAFT_07_SCHEMA_PATH = "3-schemas/TinySchemaDraft7.json";

  @Mock
  private QueryDocumentSnapshot qDocSnap;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Firestore firestore;
  @Mock
  private SchemaDataMapper schemaDataMapper;

  private ObjectMapper objectMapper = new ObjectMapper();

  private SchemaRepository schemaRepository;

  @BeforeEach
  void setUp() {
    schemaRepository = new FirestoreSchemaRepository(firestore, schemaDataMapper);
  }

  @Test
  void shouldFindSchemaDataByTitle() throws Exception {
    // given
    Date now = new Date();

    List<QueryDocumentSnapshot> documents = Collections.singletonList(qDocSnap);
    QuerySnapshot querySnapshot = QuerySnapshot
        .withDocuments(null, Timestamp.now(), documents);
    ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

    given(firestore.collection(COLLECTION_NAME).whereEqualTo("Title", SCHEMA_TITLE).get())
        .willReturn(queryFuture);
    given(qDocSnap.getString(Fields.TITLE)).willReturn(SCHEMA_TITLE);
    given(qDocSnap.getString(Fields.SCHEMA)).willReturn(getResource(DRAFT_07_SCHEMA_PATH));
    given(qDocSnap.getDate(Fields.CREATED_AT)).willReturn(now);
    given(schemaDataMapper.schemaDataDtoToSchemaData(any())).willAnswer(invocation -> {
      SchemaDataDto dto = invocation.getArgument(0);
      return SchemaData.builder()
          .title(dto.getTitle())
          .schema(objectMapper.readTree(dto.getSchema()))
          .created(now)
          .build();
    });

    // when
    SchemaData schemaData = schemaRepository.findByTitle(SCHEMA_TITLE);

    // then
    then(schemaData).isEqualTo(SchemaData.builder()
        .title(SCHEMA_TITLE)
        .schema(objectMapper.readTree(getResource(DRAFT_07_SCHEMA_PATH)))
        .created(now)
        .build());
  }

  @Test
  void shouldThrowExceptionWhenQueryFailed() {
    // given
    ApiFuture<QuerySnapshot> queryFuture =
        ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed query"));

    given(firestore.collection(COLLECTION_NAME).whereEqualTo("Title", "failed").get())
        .willReturn(queryFuture);

    // when
    Throwable thrown = catchThrowable(() -> schemaRepository.findByTitle("failed"));

    // then
    then(thrown)
        .isInstanceOf(SchemaDataQueryException.class)
        .hasRootCauseInstanceOf(IllegalArgumentException.class)
        .hasMessage("Failed to find a schema data by title failed");
  }

  @Test
  void shouldThrowExceptionWhenFutureFailed() throws Exception {
    // given
    ApiFuture queryFuture = mock(ApiFuture.class);

    given(firestore.collection(COLLECTION_NAME).whereEqualTo("Title", "failed").get())
        .willReturn(queryFuture);
    willThrow(new InterruptedException("Failed future")).given(queryFuture).get();

    // when
    Throwable thrown = catchThrowable(() -> schemaRepository.findByTitle("failed"));

    // then
    then(thrown)
        .isInstanceOf(SchemaDataQueryException.class)
        .hasRootCauseInstanceOf(InterruptedException.class)
        .hasMessage("Failed to find a schema data by title failed");
  }

  @Test
  void shouldThrowExceptionWhenItFindsFewDocuments() {
    // given
    List<QueryDocumentSnapshot> documents = Arrays.asList(qDocSnap, qDocSnap);
    QuerySnapshot querySnapshot = QuerySnapshot
        .withDocuments(null, Timestamp.now(), documents);
    ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

    given(firestore.collection(COLLECTION_NAME).whereEqualTo("Title", "double-title").get())
        .willReturn(queryFuture);

    // when
    Throwable thrown = catchThrowable(() -> schemaRepository.findByTitle("double-title"));

    // then
    then(thrown)
        .isInstanceOf(SchemaDataQueryException.class)
        .hasMessage("Find by ID returned 2 documents(s), expected 1, query ID : double-title");
  }

  @Test
  void shouldReturnNullWhenNothingWasFound() {
    // given
    List<QueryDocumentSnapshot> documents = Collections.emptyList();
    QuerySnapshot querySnapshot = QuerySnapshot
        .withDocuments(null, Timestamp.now(), documents);
    ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

    given(firestore.collection(COLLECTION_NAME).whereEqualTo("Title", "nothing").get())
        .willReturn(queryFuture);

    // when
    SchemaData schemaData = schemaRepository.findByTitle("nothing");

    // then
    then(schemaData).isNull();
  }

}
