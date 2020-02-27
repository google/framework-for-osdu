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

package org.opengroup.osdu.file.provider.gcp.repository;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FileLocation.Fields;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.exception.FileLocationNotFoundException;
import org.opengroup.osdu.file.exception.FileLocationQueryException;
import org.opengroup.osdu.file.provider.gcp.TestUtils;
import org.opengroup.osdu.file.provider.interfaces.FileLocationRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class FirestoreFileLocationRepositoryTest {

  private static final String COLLECTION_NAME = "file-locations";
  private static final String GCS_LOCATION = "gs://bucket/folder/file.tmp";
  private static final String TEMP_USER = "temp-user";

  private QueryDocumentSnapshot qDocSnap = mock(QueryDocumentSnapshot.class);
  private DocumentReference docRef = mock(DocumentReference.class);
  private DocumentSnapshot docSnap = mock(DocumentSnapshot.class);
  private Firestore firestore = mock(Firestore.class, RETURNS_DEEP_STUBS);

  private FileLocationRepository fileLocationRepository;

  @BeforeEach
  void setUp() {
    fileLocationRepository = new FirestoreFileLocationRepository(firestore);
  }

  @Nested
  class FindFileLocation {

    @Test
    void shouldFindFileLocationByFileID() {
      // given
      List<QueryDocumentSnapshot> documents = Collections.singletonList(qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME).whereEqualTo("FileID", TestUtils.FILE_ID).get())
          .willReturn(queryFuture);

      givenDocSnap(qDocSnap, getFileLocation(new Date()));

      // when
      FileLocation fileLocation = fileLocationRepository.findByFileID(TestUtils.FILE_ID);

      // then
      then(fileLocation).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenQueryFailed() {
      // given
      ApiFuture<QuerySnapshot> queryFuture =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed query"));

      given(firestore.collection(COLLECTION_NAME).whereEqualTo("FileID", TestUtils.FILE_ID).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> fileLocationRepository.findByFileID(TestUtils.FILE_ID));

      // then
      then(thrown)
          .isInstanceOf(FileLocationQueryException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasMessage("Failed to find a file location by fileID test-file-id.tmp");
    }

    @Test
    void shouldThrowExceptionWhenFutureFailed() throws Exception {
      // given
      ApiFuture queryFuture = mock(ApiFuture.class);

      given(firestore.collection(COLLECTION_NAME).whereEqualTo("FileID", TestUtils.FILE_ID).get())
          .willReturn(queryFuture);
      willThrow(new InterruptedException("Failed future")).given(queryFuture).get();

      // when
      Throwable thrown = catchThrowable(() -> fileLocationRepository.findByFileID(TestUtils.FILE_ID));

      // then
      then(thrown)
          .isInstanceOf(FileLocationQueryException.class)
          .hasRootCauseInstanceOf(InterruptedException.class)
          .hasMessage("Failed to find a file location by fileID test-file-id.tmp");
    }

    @Test
    void shouldThrowExceptionWhenItFindsFewDocuments() {
      // given
      List<QueryDocumentSnapshot> documents = Arrays.asList(qDocSnap, qDocSnap);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME).whereEqualTo("FileID", TestUtils.FILE_ID).get())
          .willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> fileLocationRepository.findByFileID(TestUtils.FILE_ID));

      // then
      then(thrown)
          .isInstanceOf(FileLocationQueryException.class)
          .hasMessage("Find by ID returned 2 documents(s), expected 1, query ID : test-file-id.tmp");
    }

    @Test
    void shouldReturnNullWhenNothingWasFound() {
      // given
      List<QueryDocumentSnapshot> documents = Collections.emptyList();
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      given(firestore.collection(COLLECTION_NAME).whereEqualTo(Fields.FILE_ID, TestUtils.FILE_ID).get())
          .willReturn(queryFuture);

      // when
      FileLocation fileLocation = fileLocationRepository.findByFileID(TestUtils.FILE_ID);

      // then
      then(fileLocation).isNull();
    }

  }

  @Nested
  class SaveFileLocation {

    @Captor
    ArgumentCaptor<Map<String, Object>> dataCaptor;

    @Test
    void shouldSaveFileLocationAndReturnSavedEntity() {
      // given
      Date createdDate = new Date();
      FileLocation fileLocation = getFileLocation(createdDate);

      ApiFuture<DocumentReference> query = ApiFutures.immediateFuture(docRef);
      ApiFuture<DocumentSnapshot> savedDoc = ApiFutures.immediateFuture(docSnap);

      given(firestore.collection(COLLECTION_NAME).add(anyMap())).willReturn(query);
      given(docRef.get()).willReturn(savedDoc);

      given(docSnap.getString(Fields.FILE_ID)).willReturn(TestUtils.FILE_ID);
      given(docSnap.getString(Fields.DRIVER)).willReturn(DriverType.GCS.name());
      given(docSnap.getString(Fields.LOCATION)).willReturn(GCS_LOCATION);
      given(docSnap.getDate(Fields.CREATED_AT)).willReturn(createdDate);
      given(docSnap.getString(Fields.CREATED_BY)).willReturn(TEMP_USER);

      // when
      FileLocation saved = fileLocationRepository.save(fileLocation);

      // then
      then(saved).isEqualTo(fileLocation);
    }

    @Test
    void shouldUseServerTimestampWhenCreateAtIsNotSpecified() {
      // given
      FileLocation fileLocation = FileLocation.builder()
          .fileID(TestUtils.FILE_ID)
          .driver(DriverType.GCS)
          .location(GCS_LOCATION)
          .createdBy(TEMP_USER)
          .build();

      ApiFuture<DocumentReference> query = ApiFutures.immediateFuture(docRef);
      ApiFuture<DocumentSnapshot> savedDoc = ApiFutures.immediateFuture(docSnap);

      given(firestore.collection(COLLECTION_NAME).add(anyMap())).willReturn(query);
      given(docRef.get()).willReturn(savedDoc);

      givenDocSnap(docSnap, fileLocation);

      // when
      FileLocation saved = fileLocationRepository.save(fileLocation);

      // then
      then(saved).isEqualToIgnoringGivenFields(saved, Fields.CREATED_AT);

      verify(firestore.collection(COLLECTION_NAME)).add(dataCaptor.capture());

      then(dataCaptor.getValue()).satisfies(map -> {
        then(map.get(Fields.FILE_ID)).isEqualTo(TestUtils.FILE_ID);
        then(map.get(Fields.CREATED_AT)).isEqualTo(FieldValue.serverTimestamp());
      });
    }

    @Test
    void shouldThrowExceptionWhenSaveQueryFailed() {
      // given
      Date createdDate = new Date();
      FileLocation fileLocation = getFileLocation(createdDate);

      ApiFuture<DocumentReference> query =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed query"));

      given(firestore.collection(COLLECTION_NAME).add(anyMap())).willReturn(query);

      // when
      Throwable thrown = catchThrowable(() -> fileLocationRepository.save(fileLocation));

      // then
      then(thrown)
          .isInstanceOf(FileLocationQueryException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Exceptions during saving  of file location");
    }

    @Test
    void shouldThrowExceptionWhenUnableToFetchSavedEntity() {
      // given
      Date createdDate = new Date();
      FileLocation fileLocation = getFileLocation(createdDate);

      ApiFuture<DocumentReference> query = ApiFutures.immediateFuture(docRef);
      ApiFuture<DocumentSnapshot> savedDoc =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed get saved"));

      given(firestore.collection(COLLECTION_NAME).add(anyMap())).willReturn(query);
      given(docRef.get()).willReturn(savedDoc);

      // when
      Throwable thrown = catchThrowable(() -> fileLocationRepository.save(fileLocation));

      // then
      then(thrown)
          .isInstanceOf(FileLocationQueryException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasMessage("Saved file location should exist");
    }

  }

  @Nested
  class FileListPagination {

    @Test
    void shouldReturnFirstPageWith2Element() {
      // given
      Query query = mock(Query.class);
      QueryDocumentSnapshot qDocSnap1 = mock(QueryDocumentSnapshot.class);
      QueryDocumentSnapshot qDocSnap2 = mock(QueryDocumentSnapshot.class);

      LocalDateTime now = LocalDateTime.now();
      FileListRequest request = FileListRequest.builder()
          .timeFrom(now.minusHours(1))
          .timeTo(now)
          .pageNum(0)
          .items((short) 5)
          .userID(TEMP_USER)
          .build();

      List<QueryDocumentSnapshot> documents = Arrays.asList(qDocSnap1, qDocSnap2);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      givenPageQuery(query, 5, 0);
      given(query.get()).willReturn(queryFuture);

      givenDocSnap(qDocSnap1, getFileLocation(toDate(now.minusMinutes(10))));
      givenDocSnap(qDocSnap2, getFileLocation(toDate(now.minusMinutes(20))));

      // when
      FileListResponse response = fileLocationRepository.findAll(request);

      // then
      then(response).isEqualToIgnoringGivenFields(FileListResponse.builder()
          .number(0)
          .numberOfElements(2)
          .size(5)
          .build(), "content");
      then(response.getContent()).hasSize(2);
    }

    @Test
    void shouldReturnSecondPageWith1Element() {
      // given
      Query query = mock(Query.class);
      QueryDocumentSnapshot qDocSnap1 = mock(QueryDocumentSnapshot.class);

      LocalDateTime now = LocalDateTime.now();
      FileListRequest request = FileListRequest.builder()
          .timeFrom(now.minusHours(1))
          .timeTo(now)
          .pageNum(1)
          .items((short) 5)
          .userID(TEMP_USER)
          .build();

      List<QueryDocumentSnapshot> documents = Collections.singletonList(qDocSnap1);
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      givenPageQuery(query, 5, 5);
      given(query.get()).willReturn(queryFuture);

      givenDocSnap(qDocSnap1, getFileLocation(toDate(now.minusMinutes(10))));

      // when
      FileListResponse response = fileLocationRepository.findAll(request);

      // then
      then(response).isEqualToIgnoringGivenFields(FileListResponse.builder()
          .number(1)
          .numberOfElements(1)
          .size(5)
          .build(), "content");
      then(response.getContent()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenNothingFoundByPageQuery() {
      // given
      Query query = mock(Query.class);

      LocalDateTime now = LocalDateTime.now();
      FileListRequest request = FileListRequest.builder()
          .timeFrom(now.minusHours(1))
          .timeTo(now)
          .pageNum(42)
          .items((short) 7)
          .userID(TEMP_USER)
          .build();

      List<QueryDocumentSnapshot> documents = Collections.emptyList();
      QuerySnapshot querySnapshot = QuerySnapshot
          .withDocuments(null, Timestamp.now(), documents);
      ApiFuture<QuerySnapshot> queryFuture = ApiFutures.immediateFuture(querySnapshot);

      givenPageQuery(query, 7, 294);
      given(query.get()).willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> fileLocationRepository.findAll(request));

      // then
      then(thrown)
          .isInstanceOf(FileLocationNotFoundException.class)
          .hasMessage("Nothing found for such filter and page(num: 42, size: 7).");
    }

    @Test
    void shouldThrowExceptionWhenPageQueryFailed() {
      // given
      Query query = mock(Query.class);

      LocalDateTime now = LocalDateTime.now();
      FileListRequest request = FileListRequest.builder()
          .timeFrom(now.minusHours(1))
          .timeTo(now)
          .pageNum(0)
          .items((short) 5)
          .userID(TEMP_USER)
          .build();

      ApiFuture<QuerySnapshot> queryFuture =
          ApiFutures.immediateFailedFuture(new IllegalArgumentException("Failed page query"));

      givenPageQuery(query, 5, 0);
      given(query.get()).willReturn(queryFuture);

      // when
      Throwable thrown = catchThrowable(() -> fileLocationRepository.findAll(request));

      // then
      then(thrown)
          .isInstanceOf(FileLocationQueryException.class)
          .hasMessageContaining("Failed to find file list page:");
    }

  }

  private FileLocation getFileLocation(Date createdDate) {
    return FileLocation.builder()
        .fileID(TestUtils.FILE_ID)
        .driver(DriverType.GCS)
        .location(GCS_LOCATION)
        .createdAt(createdDate)
        .createdBy(TEMP_USER)
        .build();
  }

  private void givenDocSnap(DocumentSnapshot qDocSnap, FileLocation fileLocation) {
    given(qDocSnap.getString(Fields.FILE_ID)).willReturn(fileLocation.getFileID());
    given(qDocSnap.getString(Fields.DRIVER)).willReturn(fileLocation.getDriver().name());
    given(qDocSnap.getString(Fields.LOCATION)).willReturn(fileLocation.getLocation());
    given(qDocSnap.getDate(Fields.CREATED_AT)).willReturn(fileLocation.getCreatedAt());
    given(qDocSnap.getString(Fields.CREATED_BY)).willReturn(fileLocation.getCreatedBy());
  }

  private void givenPageQuery(Query query, int limit, int offset) {
    given(firestore.collection(COLLECTION_NAME)
        .whereGreaterThanOrEqualTo(eq(Fields.CREATED_AT), any(Date.class))
        .whereLessThanOrEqualTo(eq(Fields.CREATED_AT), any(Date.class))
        .whereEqualTo(eq(Fields.CREATED_BY), eq(TEMP_USER))
        .orderBy(Fields.CREATED_AT)
        .limit(limit)
        .offset(offset))
        .willReturn(query);
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.toInstant(ZoneOffset.UTC));
  }

}
