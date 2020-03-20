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

package org.opengroup.osdu.delivery.provider.gcp.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.delivery.ReplaceCamelCase;
import org.opengroup.osdu.delivery.model.SignedObject;
import org.opengroup.osdu.delivery.model.SignedUrl;
import org.opengroup.osdu.delivery.provider.gcp.TestUtils;
import org.opengroup.osdu.delivery.provider.gcp.model.property.FileLocationProperties;
import org.opengroup.osdu.delivery.provider.interfaces.StorageRepository;
import org.opengroup.osdu.delivery.provider.interfaces.StorageService;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class StorageServiceImplTest {

  @Mock
  private StorageRepository storageRepository;

  @Captor
  ArgumentCaptor<String> filenameCaptor;

  private StorageService storageService;

  @BeforeEach
  void setUp() {
    FileLocationProperties fileLocationProperties
        = new FileLocationProperties(TestUtils.BUCKET_NAME, TestUtils.USER_DES_ID);
    storageService = new StorageServiceImpl(fileLocationProperties, storageRepository);
  }

  @Test
  void shouldCreateObjectSignedUrl() {
    // given
    SignedObject signedObject = getSignedObject();
    given(storageRepository.createSignedObject(eq(TestUtils.BUCKET_NAME), anyString())).willReturn(signedObject);

    // when
    SignedUrl signedUrl = storageService.createSignedUrl(
        TestUtils.FILE_ID, TestUtils.AUTHORIZATION_TOKEN, TestUtils.PARTITION);

    // then
    then(signedUrl).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.GCS_URL_CONDITION);
      then(url.getUri().toString()).matches(TestUtils.GCS_OBJECT_URI);
      then(url.getCreatedAt()).isBefore(now());
      then(url.getCreatedBy()).isEqualTo(TestUtils.USER_DES_ID);
    });

    verify(storageRepository).createSignedObject(eq(TestUtils.BUCKET_NAME), filenameCaptor.capture());
    then(filenameCaptor.getValue()).matches(TestUtils.USER_DES_ID + ".*?" + TestUtils.UUID_REGEX);
  }

  @Test
  void shouldThrowExceptionWhenResultFilepathIsMoreThan1024Characters() {
    // given
    String fileId = RandomStringUtils.randomAlphanumeric(1024);

    // when
    Throwable thrown = catchThrowable(() -> storageService.createSignedUrl(fileId,
        TestUtils.AUTHORIZATION_TOKEN, TestUtils.PARTITION));

    // then
    then(thrown)
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("The maximum filepath length is 1024 characters");
    verify(storageRepository, never()).createSignedObject(anyString(), anyString());
  }

  private SignedObject getSignedObject() {
    String bucketName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = TestUtils.USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);
    String filename = TestUtils.getUuidString();

    URI uri = TestUtils.getGcsObjectUri(bucketName, folderName, filename);
    URL url = TestUtils.getGcsObjectUrl(bucketName, folderName, filename);

    return SignedObject.builder()
        .uri(uri)
        .url(url)
        .build();
  }

  private Instant now() {
    return Instant.now(Clock.systemUTC());
  }

}
