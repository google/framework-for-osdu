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

package org.opengroup.osdu.ingest.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.opengroup.osdu.ingest.TestUtils.getFeignRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.client.FileServiceClient;
import org.opengroup.osdu.ingest.exception.OsduServerErrorException;
import org.opengroup.osdu.ingest.model.Headers;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class FileIntegrationServiceTest {

  private static final String FILE_LOCATION = "file-location";
  private static final String GCP = "GCP";
  private static final String TEST_AUTH_TOKEN = "test-auth-token";
  private static final String TEST_PARTITION = "test-partition";
  private static final String FILE_ID = "fileId";
  private ObjectMapper mapper = new ObjectMapper();

  @Mock
  private FileServiceClient fileServiceClient;

  @Captor
  ArgumentCaptor<FileLocationRequest> fileLocationRequestCaptor;

  FileIntegrationService fileIntegrationService;

  @BeforeEach
  void setUp() {
    fileIntegrationService = new FileIntegrationService(fileServiceClient, mapper);
  }

  @Test
  void shouldGetFileInfo() throws JsonProcessingException {

    // given
    Headers requestHeaders = Headers.builder()
        .authorizationToken(TEST_AUTH_TOKEN)
        .partitionID(TEST_PARTITION)
        .build();

    FileLocationResponse fileLocationResponse = FileLocationResponse.builder().driver(GCP)
        .location(FILE_LOCATION)
        .build();

    Response response = Response.builder()
        .body(mapper.writeValueAsString(fileLocationResponse), StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(HttpStatus.OK.value()).build();
    given(fileServiceClient.getFileLocation(eq(TEST_AUTH_TOKEN), eq(TEST_PARTITION), any()))
        .willReturn(response);

    // when
    FileLocationResponse fileLocationResponseActual = fileIntegrationService
        .getFileInfo(FILE_ID, requestHeaders);

    // then
    then(fileLocationResponseActual.getDriver()).isEqualTo(GCP);
    then(fileLocationResponseActual.getLocation()).isEqualTo(FILE_LOCATION);
    verify(fileServiceClient).getFileLocation(eq(TEST_AUTH_TOKEN), eq(TEST_PARTITION),
        fileLocationRequestCaptor.capture());
    then(fileLocationRequestCaptor.getValue().getFileID()).isEqualTo(FILE_ID);
  }

  @Test
  void shouldThrowExceptionIfResponseIsEmpty() throws JsonProcessingException {

    // given
    Headers requestHeaders = Headers.builder()
        .authorizationToken(TEST_AUTH_TOKEN)
        .partitionID(TEST_PARTITION)
        .build();

    FileLocationResponse fileLocationResponse = FileLocationResponse.builder().driver(null)
        .location(null)
        .build();

    Response response = Response.builder()
        .body(mapper.writeValueAsString(fileLocationResponse), StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(HttpStatus.OK.value()).build();
    given(fileServiceClient.getFileLocation(eq(TEST_AUTH_TOKEN), eq(TEST_PARTITION), any()))
        .willReturn(response);

    // when
    Throwable thrown = catchThrowable(() -> fileIntegrationService
        .getFileInfo(FILE_ID, requestHeaders));

    // then
    then(thrown).isInstanceOf(OsduServerErrorException.class);
    then(thrown.getMessage()).isEqualTo("No file location in file service response");
  }
}
