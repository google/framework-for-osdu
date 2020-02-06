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

package com.osdu.service.google;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockHttpTransport.Builder;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.ByteStreams;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import com.osdu.ReplaceCamelCase;
import com.osdu.exception.IngestException;
import com.osdu.exception.OsduException;
import com.osdu.model.property.CloudStorageProperties;
import com.osdu.service.StorageService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class GcpStorageServiceTest {

  private static final String TEMP_LOCATION_BUCKET = "temp-location";
  private static final String FILE_IN_RESOURCE = "test.txt";
  private static final String FILENAME_1 = "file-1.txt";
  private static final String SIGNED_URL_VALUE =
      "http://signed.url/" + FILENAME_1 + "?AccessId=datafier@email.com&Expires=123&Signature=lX";

  private Storage localStorage = LocalStorageHelper.getOptions().getService();
  private Storage googleCloudStorage;
  @Mock
  private CustomMediaHttpUploader uploader;
  @Captor
  private ArgumentCaptor<BlobId> blobIdCaptor;

  private CloudStorageProperties storageProperties = CloudStorageProperties.builder()
      .tempLocationBucket(TEMP_LOCATION_BUCKET)
      .build();

  private StorageService storageService;

  @BeforeEach
  void setUp() {
    googleCloudStorage = Mockito.mock(Storage.class, AdditionalAnswers.delegatesTo(localStorage));
    storageService = new GcpStorageService(googleCloudStorage, uploader, storageProperties);
  }

  @Test
  void shouldUploadFileToStorage() {
    // given
    URL resource = GcpStorageServiceTest.class.getResource(FILE_IN_RESOURCE);

    // when
    Blob blob = storageService.uploadFileToStorage(resource, FILE_IN_RESOURCE);

    // then
    then(blob.getContentType()).isEqualTo(MediaType.TEXT_PLAIN_VALUE);
    then(blob.getGeneratedId()).containsPattern("temp-location/.{36}/test.txt");

    InOrder inOrder = inOrder(googleCloudStorage, uploader);
    inOrder.verify(googleCloudStorage).create(any(BlobInfo.class));
    inOrder.verify(googleCloudStorage).get(eq(TEMP_LOCATION_BUCKET), any(String.class));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldWriteFileToSignedUrlLocation() throws IOException {
    // given
    Blob testBlob = writeToLocalStorage();
    URL signedUrl = new URL(SIGNED_URL_VALUE);
    GenericUrl genericUrl = new GenericUrl(signedUrl);

    HttpResponse response = getMockHttpResponse(genericUrl, HttpStatus.OK);

    given(uploader.resumableUpload(any(InputStreamContent.class), eq(genericUrl)))
        .willReturn(response);

    // when
    HttpResponse httpResponse = storageService.writeFileToSignedUrlLocation(testBlob, signedUrl);

    // then
    then(httpResponse).isNotNull();

    InOrder inOrder = inOrder(googleCloudStorage, uploader);
    inOrder.verify(googleCloudStorage).reader(blobIdCaptor.capture());
    inOrder.verify(uploader).resumableUpload(any(), eq(genericUrl));
    inOrder.verifyNoMoreInteractions();

    then(blobIdCaptor.getValue())
        .isEqualTo(BlobId.of(TEMP_LOCATION_BUCKET, FILENAME_1, 1L));
  }

  @Test
  public void shouldThrownIngestExceptionWhenUnableToUploadFileBySignedUrl() throws IOException {
    // given
    Blob testBlob = writeToLocalStorage();
    URL signedUrl = new URL(SIGNED_URL_VALUE);
    GenericUrl genericUrl = new GenericUrl(signedUrl);

    willThrow(IOException.class).given(uploader)
        .resumableUpload(any(InputStreamContent.class), eq(genericUrl));

    // when
    Throwable thrown = catchThrowable(() -> storageService.writeFileToSignedUrlLocation(testBlob, signedUrl));

    // then
    then(thrown)
        .isInstanceOf(IngestException.class)
        .hasMessage(format("Error during upload file: %s from cloud storage to signed url location: %s",
            FILENAME_1, SIGNED_URL_VALUE));

    InOrder inOrder = inOrder(googleCloudStorage, uploader);
    inOrder.verify(googleCloudStorage).reader(blobIdCaptor.capture());
    inOrder.verify(uploader).resumableUpload(any(), eq(genericUrl));
    inOrder.verifyNoMoreInteractions();

    then(blobIdCaptor.getValue())
        .isEqualTo(BlobId.of(TEMP_LOCATION_BUCKET, FILENAME_1, 1L));
  }

  @Test
  public void shouldThrownOsduExceptionWhenReceiveUnsuccessfullyStatusCode() throws IOException {
    // given
    Blob testBlob = writeToLocalStorage();
    URL signedUrl = new URL(SIGNED_URL_VALUE);
    GenericUrl genericUrl = new GenericUrl(signedUrl);

    HttpResponse response = getMockHttpResponse(genericUrl, HttpStatus.BAD_REQUEST);

    given(uploader.resumableUpload(any(InputStreamContent.class), eq(genericUrl)))
        .willReturn(response);

    // when
    Throwable thrown = catchThrowable(() -> storageService.writeFileToSignedUrlLocation(testBlob, signedUrl));

    // then
    then(thrown)
        .isInstanceOf(OsduException.class)
        .hasMessage("Not success status of signed url file upload request. Status: 400"
            + "\n message: Bad Request");

    InOrder inOrder = inOrder(googleCloudStorage, uploader);
    inOrder.verify(googleCloudStorage).reader(blobIdCaptor.capture());
    inOrder.verify(uploader).resumableUpload(any(), eq(genericUrl));
    inOrder.verifyNoMoreInteractions();

    then(blobIdCaptor.getValue())
        .isEqualTo(BlobId.of(TEMP_LOCATION_BUCKET, FILENAME_1, 1L));
  }

  private Blob writeToLocalStorage() throws IOException {
    Blob blob = localStorage
        .create(BlobInfo.newBuilder(BlobId.of(TEMP_LOCATION_BUCKET, FILENAME_1))
            .setContentType(MediaType.TEXT_PLAIN_VALUE)
            .build());
    InputStream inputStream = GcpStorageServiceTest.class.getResourceAsStream(FILE_IN_RESOURCE);
    try (BufferedInputStream in = new BufferedInputStream(inputStream);
        WriteChannel writer = googleCloudStorage.writer(blob)) {
      ByteStreams.copy(in, Channels.newOutputStream(writer));

    }

    return localStorage.get(TEMP_LOCATION_BUCKET, FILENAME_1);
  }

  private HttpResponse getMockHttpResponse(GenericUrl genericUrl, HttpStatus status) throws IOException {
    MockLowLevelHttpResponse lowLevelHttpResponse = new MockLowLevelHttpResponse();
    lowLevelHttpResponse.setStatusCode(status.value());
    lowLevelHttpResponse.setReasonPhrase(status.getReasonPhrase());
    MockHttpTransport transport = new Builder()
        //.setLowLevelHttpRequest()
        .setLowLevelHttpResponse(lowLevelHttpResponse)
        .build();
    HttpRequest request = transport.createRequestFactory().buildPutRequest(genericUrl, null);
    request.setThrowExceptionOnExecuteError(false);
    return request.execute();
  }
}