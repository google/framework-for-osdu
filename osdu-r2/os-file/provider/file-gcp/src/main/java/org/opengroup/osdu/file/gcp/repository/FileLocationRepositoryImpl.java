/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.file.gcp.repository;

import static java.lang.String.format;
import static org.opengroup.osdu.core.common.model.file.FileLocation.Fields.CREATED_AT;
import static org.opengroup.osdu.core.common.model.file.FileLocation.Fields.CREATED_BY;
import static org.opengroup.osdu.core.common.model.file.FileLocation.Fields.DRIVER;
import static org.opengroup.osdu.core.common.model.file.FileLocation.Fields.FILE_ID;
import static org.opengroup.osdu.core.common.model.file.FileLocation.Fields.LOCATION;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FilesListRequest;
import org.opengroup.osdu.core.common.model.file.FilesListResponse;
import org.opengroup.osdu.file.exception.FileLocationNotFoundException;
import org.opengroup.osdu.file.exception.FileLocationQueryException;
import org.opengroup.osdu.file.repository.FileLocationRepository;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FileLocationRepositoryImpl implements FileLocationRepository {

  private static final String COLLECTION_NAME = "file-locations";

  final Firestore firestore;

  @Override
  public FileLocation findByFileID(String fileID) {
    log.debug("Requesting file location. File ID : {}", fileID);
    ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereEqualTo(FILE_ID, fileID).get();

    QuerySnapshot querySnapshot = getSafety(query,
        "Failed to find a file location by fileID " + fileID);

    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new FileLocationQueryException(
          format("Find by ID returned %s documents(s), expected 1, query ID : %s",
              documents.size(), fileID));
    }

    FileLocation fileLocation = documents.isEmpty()
        ? null
        : buildFileLocation(documents.get(0));

    log.debug("Found file location : {}", fileLocation);
    return fileLocation;
  }

  @Override
  public FileLocation save(FileLocation fileLocation) {
    log.info("Saving file location : {}", fileLocation);
    final String errorMsg = "Exceptions during saving  of file location: " + fileLocation;

    Map<String, Object> data = getFileLocationData(fileLocation);

    ApiFuture<DocumentReference> query = firestore.collection(COLLECTION_NAME).add(data);
    DocumentReference addedDocRef = getSafety(query, errorMsg);
    log.info("Fetch DocumentReference pointing to a new document with an auto-generated ID : {}",
        addedDocRef);

    DocumentSnapshot saved = getSafety(addedDocRef.get(),
        "Saved file location should exist");
    log.info("Fetch saved file location : {}", saved);
    return buildFileLocation(saved);
  }

  @Override
  public FilesListResponse findAll(FilesListRequest request) {
    int pageSize = request.getItems();
    int pageNum = request.getPageNum();
    int skips = pageSize * pageNum;
    Query query = firestore.collection(COLLECTION_NAME)
        .whereGreaterThanOrEqualTo(CREATED_AT, toDate(request.getTimeFrom()))
        .whereLessThanOrEqualTo(CREATED_AT, toDate(request.getTimeTo()))
        .whereEqualTo(CREATED_BY, request.getUserID())
        .orderBy(CREATED_AT)
        .limit(pageSize)
        .offset(skips);

    QuerySnapshot page = getSafety(query.get(),
        "Failed to find files list page: " + request);
    if (page.isEmpty()) {
      throw new FileLocationNotFoundException(
          format("Nothing found for such filter and page(num: %s, size: %s).", pageNum, pageSize));
    }
    List<FileLocation> content = page.getDocuments().stream()
        .map(this::buildFileLocation)
        .collect(Collectors.toList());
    return FilesListResponse.builder()
        .content(content)
        .size(pageSize)
        .number(pageNum)
        .numberOfElements(content.size())
        .build();
  }

  private <T> T getSafety(Future<T> future, String errorMsg) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new FileLocationQueryException(errorMsg, e);
    } catch (ExecutionException e) {
      throw new FileLocationQueryException(errorMsg, e);
    }
  }

  private FileLocation buildFileLocation(DocumentSnapshot snap) {
    log.info("Build file location. Document snapshot : {}", snap.getData());
    return FileLocation.builder()
        .fileID(snap.getString(FILE_ID))
        .driver(DriverType.valueOf(snap.getString(DRIVER)))
        .location(snap.getString(LOCATION))
        .createdAt(snap.getDate(CREATED_AT))
        .createdBy(snap.getString(CREATED_BY))
        .build();
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.toInstant(ZoneOffset.UTC));
  }

  private Map<String, Object> getFileLocationData(FileLocation fileLocation) {
    Object createdAt = ObjectUtils.defaultIfNull(fileLocation.getCreatedAt(),
        FieldValue.serverTimestamp());

    Map<String, Object> data = new HashMap<>();
    data.put(FILE_ID, fileLocation.getFileID());
    data.put(DRIVER, fileLocation.getDriver().name());
    data.put(LOCATION, fileLocation.getLocation());
    data.put(CREATED_AT, createdAt);
    data.put(CREATED_BY, fileLocation.getCreatedBy());
    return data;
  }

}
