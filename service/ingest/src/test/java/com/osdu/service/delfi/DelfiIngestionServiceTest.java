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

package com.osdu.service.delfi;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.google.cloud.storage.Blob;
import com.osdu.ReplaceCamelCase;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.client.delfi.RecordDataFields;
import com.osdu.exception.IngestException;
import com.osdu.exception.OsduBadRequestException;
import com.osdu.model.Record;
import com.osdu.model.RequestContext;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.model.type.base.OsduObject;
import com.osdu.model.type.file.FileData;
import com.osdu.model.type.file.FileGroupTypeProperties;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.service.IngestionService;
import com.osdu.service.JsonUtils;
import com.osdu.service.PortalService;
import com.osdu.service.StorageService;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
public class DelfiIngestionServiceTest {

  private static final String APP_KEY = "appKey";
  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String EXTERNAL_FILE_STORAGE = "http://some.host.com";
  private static final String FILE_ASSOCIATIVE_ID_1 = "file-id-1";
  private static final String STORAGE_HREF = "http://storage.host.com";

  @Mock
  private DelfiIngestionClient delfiIngestionClient;
  @Mock
  private StorageService storageService;
  @Mock
  private PortalService portalService;

  private DelfiPortalProperties portalProperties = DelfiPortalProperties.builder()
      .appKey(APP_KEY)
      .build();

  private IngestionService ingestionService;

  @BeforeEach
  public void setUp() {
    ingestionService = new DelfiIngestionService(portalProperties, delfiIngestionClient,
        storageService, portalService);
  }

  @Test
  public void shouldUploadFileToDelfiLandingZone() throws Exception {
    // given
    String fileName = "file.las";

    String fileExternalHref = EXTERNAL_FILE_STORAGE + "/" + fileName;
    URL fileExternalUrl = new URL(fileExternalHref);

    String relativeFilePath = "/some-landing-zone/some-user/uuid" + "/" + fileName;
    URL fileLocationUrl = new URL(STORAGE_HREF + relativeFilePath);

    ManifestFile file = getManifestFile(fileExternalHref);

    Blob blob = mock(Blob.class);
    given(storageService.uploadFileToStorage(fileExternalUrl, fileName))
        .willReturn(blob);
    given(delfiIngestionClient.getSignedUrlForLocation(fileName, AUTHORIZATION_TOKEN,
        APP_KEY, PARTITION)).willReturn(SignedUrlResult.builder()
        .responseCode(201)
        .locationUrl(fileLocationUrl)
        .relativeFilePath(relativeFilePath)
        .build());

    // when
    SignedFile signedFile = ingestionService.uploadFile(file, AUTHORIZATION_TOKEN, PARTITION);

    // then
    then(signedFile).isEqualTo(SignedFile.builder()
        .file(file)
        .locationUrl(fileLocationUrl)
        .relativeFilePath(relativeFilePath)
        .build());

    InOrder inOrder = inOrder(portalService, storageService, delfiIngestionClient);

    inOrder.verify(storageService).uploadFileToStorage(fileExternalUrl, fileName);
    inOrder.verify(delfiIngestionClient)
        .getSignedUrlForLocation(fileName, AUTHORIZATION_TOKEN, APP_KEY, PARTITION);
    inOrder.verify(storageService)
        .writeFileToSignedUrlLocation(blob, fileLocationUrl);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldThrowIngestExceptionWhenPreloadFilePathIsInvalid() throws Exception {
    // given
    String fileExternalHref = "invalid-file-url";

    ManifestFile file = getManifestFile(fileExternalHref);

    // when
    Throwable thrown = catchThrowable(
        () -> ingestionService.uploadFile(file, AUTHORIZATION_TOKEN, PARTITION));

    // then
    then(thrown)
        .isInstanceOf(IngestException.class)
        .hasMessage("Could not create URL from preload file path: invalid-file-url");

    InOrder inOrder = inOrder(portalService, storageService, delfiIngestionClient);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldThrowIngestExceptionWhenPreloadFilePathDoesNotContainFilename() throws Exception {
    // given
    String fileExternalHref = EXTERNAL_FILE_STORAGE;
    URL fileExternalUrl = new URL(fileExternalHref);

    String relativeFilePath = "/some-landing-zone/some-user/uuid";
    URL fileLocationUrl = new URL(STORAGE_HREF + relativeFilePath);

    ManifestFile file = getManifestFile(fileExternalHref);

    // when
    Throwable thrown = catchThrowable(
        () -> ingestionService.uploadFile(file, AUTHORIZATION_TOKEN, PARTITION));

    // then
    then(thrown)
        .isInstanceOf(IngestException.class)
        .hasMessage("File name obtained is empty, URL : http://some.host.com");

    InOrder inOrder = inOrder(portalService, storageService, delfiIngestionClient);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldThrowIngestExceptionWhenUnableToGetSignedUrlInLandingZone() throws Exception {
    // given
    String fileName = "invalid-file";

    String fileExternalHref = EXTERNAL_FILE_STORAGE + "/" + fileName;
    URL fileExternalUrl = new URL(fileExternalHref);

    String relativeFilePath = "/some-landing-zone/some-user/uuid" + "/" + fileName;
    URL fileLocationUrl = new URL(STORAGE_HREF + relativeFilePath);

    ManifestFile file = getManifestFile(fileExternalHref);

    Blob blob = mock(Blob.class);
    given(storageService.uploadFileToStorage(fileExternalUrl, fileName))
        .willReturn(blob);
    given(delfiIngestionClient.getSignedUrlForLocation(fileName, AUTHORIZATION_TOKEN,
        APP_KEY, PARTITION)).willReturn(SignedUrlResult.builder()
        .responseCode(HttpStatus.BAD_REQUEST.value())
        .build());

    // when
    Throwable thrown = catchThrowable(
        () -> ingestionService.uploadFile(file, AUTHORIZATION_TOKEN, PARTITION));

    // then
    then(thrown)
        .isInstanceOf(IngestException.class)
        .hasMessage("Count not fetch a signed URL to landing zone for file: invalid-file");

    InOrder inOrder = inOrder(portalService, storageService, delfiIngestionClient);
    inOrder.verify(storageService)
        .uploadFileToStorage(fileExternalUrl, fileName);
    inOrder.verify(delfiIngestionClient)
        .getSignedUrlForLocation(fileName, AUTHORIZATION_TOKEN, APP_KEY, PARTITION);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldMarkRecordsAsFailedAndSaveThem() {
    // given
    List<Record> records = Arrays.asList(getDelfiRecord(), getDelfiRecord(), getDelfiRecord());
    RequestContext requestContext = RequestContext.builder()
        .authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION)
        .build();

    given(portalService.putRecord(any(Record.class), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .willAnswer(this::getFailRecordAnswer);

    // when
    List<Record> failedRecords = ingestionService.failRecords(records, requestContext);

    // then
    then(failedRecords)
        .hasSize(3)
        .extracting("data.osdu.ResourceLifecycleStatus", String.class)
        .containsOnly("srn:reference-data/ResourceLifecycleStatus:RESCINDED:");

    InOrder inOrder = inOrder(portalService, storageService, delfiIngestionClient);
    inOrder.verify(portalService, times(3))
        .putRecord(any(Record.class), eq(AUTHORIZATION_TOKEN), eq(PARTITION));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldFallThroughExceptionDuringFailingRecords() {
    // given
    List<Record> records = Arrays.asList(getDelfiRecord(), getDelfiRecord(), getDelfiRecord());
    RequestContext requestContext = RequestContext.builder()
        .authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION)
        .build();

    given(portalService.putRecord(any(Record.class), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .willAnswer(this::getFailRecordAnswer)
        .willThrow(new OsduBadRequestException("bad request"));

    // when
    Throwable thrown = catchThrowable(() -> ingestionService.failRecords(records, requestContext));

    // then
    then(thrown)
        .isInstanceOf(OsduBadRequestException.class)
        .hasMessage("bad request");

    InOrder inOrder = inOrder(portalService, storageService, delfiIngestionClient);
    inOrder.verify(portalService, times(2))
        .putRecord(any(Record.class), eq(AUTHORIZATION_TOKEN), eq(PARTITION));
    inOrder.verifyNoMoreInteractions();
  }

  private ManifestFile getManifestFile(String fileHref) {
    FileGroupTypeProperties fileGroupTypeProperties = new FileGroupTypeProperties();
    fileGroupTypeProperties.setPreLoadFilePath(fileHref);

    FileData fileData = new FileData();
    fileData.setGroupTypeProperties(fileGroupTypeProperties);

    ManifestFile file = new ManifestFile();
    file.setAssociativeId(FILE_ASSOCIATIVE_ID_1);
    file.setData(fileData);

    return file;
  }

  private Record getDelfiRecord() {
    OsduObject osduObject = new OsduObject();
    osduObject.setResourceID("srn:file/las2:" + RandomStringUtils.randomAlphabetic(32) + ":1");
    osduObject.setResourceLifecycleStatus("srn:reference-data/ResourceLifecycleStatus:CREATED:");

    Map<String, Object> data = new HashMap<>();
    data.put(RecordDataFields.OSDU_DATA, osduObject);

    return DelfiRecord.builder()
        .data(data)
        .build();
  }

  private Record getFailRecordAnswer(InvocationOnMock invocation) {
    Record record = invocation.getArgument(0);
    return JsonUtils.deepCopy(record, Record.class);
  }

}