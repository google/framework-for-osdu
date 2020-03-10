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

package org.opengroup.osdu.delivery.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.opengroup.osdu.delivery.TestUtils.AUTHORIZATION_TOKEN;
import static org.opengroup.osdu.delivery.TestUtils.FILE_ID;
import static org.opengroup.osdu.delivery.TestUtils.PARTITION;
import static org.opengroup.osdu.delivery.TestUtils.SIGNED_URL_CONDITION;
import static org.opengroup.osdu.delivery.TestUtils.SRG_OBJECT_URI;
import static org.opengroup.osdu.delivery.TestUtils.SRG_PROTOCOL;
import static org.opengroup.osdu.delivery.TestUtils.USER_DES_ID;
import static org.opengroup.osdu.delivery.TestUtils.UUID_CONDITION;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.delivery.ReplaceCamelCase;
import org.opengroup.osdu.delivery.TestUtils;
import org.opengroup.osdu.delivery.exception.FileLocationNotFoundException;
import org.opengroup.osdu.delivery.exception.LocationAlreadyExistsException;
import org.opengroup.osdu.delivery.exception.OsduUnauthorizedException;
import org.opengroup.osdu.delivery.mapper.HeadersMapper;
import org.opengroup.osdu.delivery.model.Headers;
import org.opengroup.osdu.delivery.model.SignedUrl;
import org.opengroup.osdu.delivery.provider.interfaces.AuthenticationService;
import org.opengroup.osdu.delivery.provider.interfaces.FileLocationRepository;
import org.opengroup.osdu.delivery.provider.interfaces.LocationMapper;
import org.opengroup.osdu.delivery.provider.interfaces.LocationService;
import org.opengroup.osdu.delivery.provider.interfaces.StorageService;
import org.opengroup.osdu.delivery.provider.interfaces.ValidationService;
import org.opengroup.osdu.delivery.util.JsonUtils;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class LocationServiceImplTest {

  private static final String SIGNED_URL_KEY = "SignedURL";

  @Spy
  private HeadersMapper headersMapper = Mappers.getMapper(HeadersMapper.class);
  @Mock
  private LocationMapper locationMapper;
  @Mock
  private AuthenticationService authenticationService;
  @Mock
  private ValidationService validationService;
  @Mock
  private FileLocationRepository fileLocationRepository;
  @Mock
  private StorageService storageService;

  @Captor
  private ArgumentCaptor<String> fileIdCaptor;
  @Captor
  private ArgumentCaptor<FileLocation> fileLocationCaptor;

  private LocationService locationService;

  @BeforeEach
  void setUp() {
    locationService = new LocationServiceImpl(headersMapper, locationMapper, authenticationService,
        validationService, fileLocationRepository, storageService);
  }

  @Nested
  class GetLocation {

    @Test
    void shouldCreateLocationAndGenerateFileIdAsUuidWhenFileIdIsNotProvided() {
      // given
      LocationRequest request = LocationRequest.builder()
          .build();
      MessageHeaders headers = getMessageHeaders();

      given(storageService.createSignedUrl(anyString(), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
          .willReturn(getSignedUrl());
      given(fileLocationRepository.save(any())).willAnswer(this::getFileLocationAnswer);
      given(locationMapper.buildLocationResponse(any(SignedUrl.class), any(FileLocation.class)))
          .willAnswer(LocationServiceImplTest::getLocationResponseAnswer);

      // when
      LocationResponse locationResponse = locationService.getLocation(request, headers);

      // then
      then(locationResponse.getFileID()).satisfies(UUID_CONDITION);
      then(locationResponse.getLocation())
          .hasSize(1)
          .hasEntrySatisfying(SIGNED_URL_KEY, SIGNED_URL_CONDITION);

      InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
          fileLocationRepository, storageService);
      inOrder.verify(headersMapper).toHeaders(headers);
      inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verify(validationService).validateLocationRequest(request);
      inOrder.verify(fileLocationRepository).findByFileID(isNull());
      inOrder.verify(storageService)
          .createSignedUrl(fileIdCaptor.capture(), eq(AUTHORIZATION_TOKEN), eq(
              PARTITION));
      inOrder.verify(fileLocationRepository).save(fileLocationCaptor.capture());
      inOrder.verifyNoMoreInteractions();

      then(fileIdCaptor.getValue()).satisfies(UUID_CONDITION);
      then(fileLocationCaptor.getValue()).satisfies(fileLocation -> {
        then(fileLocation.getFileID()).satisfies(UUID_CONDITION);
        then(fileLocation.getDriver()).isEqualTo(DriverType.GCS);
        then(fileLocation.getLocation()).matches(SRG_OBJECT_URI);
        then(fileLocation.getCreatedBy()).isNotEmpty();
        then(fileLocation.getCreatedAt()).isBefore(Date.from(TestUtils.now()));
      });
    }

    @Test
    void shouldCreateLocationUsingProvidedFileId() {
      // given
      LocationRequest request = LocationRequest.builder()
          .fileID(FILE_ID)
          .build();
      MessageHeaders headers = getMessageHeaders();

      given(storageService.createSignedUrl(FILE_ID, AUTHORIZATION_TOKEN, PARTITION))
          .willReturn(getSignedUrl());
      given(fileLocationRepository.save(any())).willAnswer(this::getFileLocationAnswer);
      given(locationMapper.buildLocationResponse(any(SignedUrl.class), any(FileLocation.class)))
          .willAnswer(LocationServiceImplTest::getLocationResponseAnswer);

      // when
      LocationResponse locationResponse = locationService.getLocation(request, headers);

      // then
      then(locationResponse.getFileID()).isEqualTo(FILE_ID);
      then(locationResponse.getLocation())
          .hasSize(1)
          .hasEntrySatisfying(SIGNED_URL_KEY, SIGNED_URL_CONDITION);

      InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
          fileLocationRepository, storageService);
      inOrder.verify(headersMapper).toHeaders(headers);
      inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verify(validationService).validateLocationRequest(request);
      inOrder.verify(fileLocationRepository).findByFileID(FILE_ID);
      inOrder.verify(storageService).createSignedUrl(FILE_ID, AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verify(fileLocationRepository).save(fileLocationCaptor.capture());
      inOrder.verifyNoMoreInteractions();

      then(fileLocationCaptor.getValue()).satisfies(fileLocation -> {
        then(fileLocation.getFileID()).isEqualTo(FILE_ID);
        then(fileLocation.getDriver()).isEqualTo(DriverType.GCS);
        then(fileLocation.getLocation()).matches(SRG_OBJECT_URI);
        then(fileLocation.getCreatedBy()).isNotEmpty();
        then(fileLocation.getCreatedAt()).isBefore(Date.from(TestUtils.now()));
      });
    }

    @Test
    void shouldThrowExceptionForGetLocationWhenCheckingAuthenticationIsFailed() {
      // given
      LocationRequest request = LocationRequest.builder()
          .build();
      MessageHeaders headers = getMessageHeaders();

      willThrow(OsduUnauthorizedException.class).given(authenticationService)
          .checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);

      // when
      Throwable thrown = catchThrowable(() -> locationService.getLocation(request, headers));

      // then
      then(thrown).isInstanceOf(OsduUnauthorizedException.class);

      InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
          fileLocationRepository, storageService);
      inOrder.verify(headersMapper).toHeaders(headers);
      inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowExceptionWhenGetLocationRequestIsInvalid() {
      // given
      LocationRequest request = LocationRequest.builder()
          .fileID("^&$%%id.")
          .build();
      MessageHeaders headers = getMessageHeaders();

      willThrow(ConstraintViolationException.class).given(validationService)
          .validateLocationRequest(request);

      // when
      Throwable thrown = catchThrowable(() -> locationService.getLocation(request, headers));

      // then
      then(thrown).isInstanceOf(ConstraintViolationException.class);

      InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
          fileLocationRepository, storageService);
      inOrder.verify(headersMapper).toHeaders(headers);
      inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verify(validationService).validateLocationRequest(request);
      inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowExceptionWhenLocationAlreadyExistsForProvidedFileID() {
      // given
      LocationRequest request = LocationRequest.builder()
          .fileID(FILE_ID)
          .build();
      MessageHeaders headers = getMessageHeaders();

      given(fileLocationRepository.findByFileID(FILE_ID)).willReturn(FileLocation.builder().build());

      // when
      Throwable thrown = catchThrowable(() -> locationService.getLocation(request, headers));

      // then
      then(thrown).isInstanceOf(LocationAlreadyExistsException.class);

      InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
          fileLocationRepository, storageService);
      inOrder.verify(headersMapper).toHeaders(headers);
      inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verify(validationService).validateLocationRequest(request);
      inOrder.verify(fileLocationRepository).findByFileID(FILE_ID);
      inOrder.verifyNoMoreInteractions();
    }

    private FileLocation getFileLocationAnswer(InvocationOnMock invocation) {
      return JsonUtils.deepCopy(invocation.getArgument(0), FileLocation.class);
    }

  }

  @Nested
  class GetFileLocation {

    @Test
    void shouldReturnFileLocationForProvidedFileID() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(FILE_ID)
          .build();
      MessageHeaders headers = getMessageHeaders();

      given(fileLocationRepository.findByFileID(FILE_ID)).willReturn(getFileLocation());

      // when
      FileLocationResponse response = locationService.getFileLocation(request, headers);

      // then
      then(response.getDriver()).isEqualTo(DriverType.GCS);
      then(response.getLocation()).matches(SRG_OBJECT_URI);
    }

    @Test
    void shouldThrowExceptionForGetFileLocationWhenCheckingAuthenticationIsFailed() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(FILE_ID)
          .build();
      MessageHeaders headers = getMessageHeaders();

      willThrow(OsduUnauthorizedException.class).given(authenticationService)
          .checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);

      // when
      Throwable thrown = catchThrowable(() -> locationService.getFileLocation(request, headers));

      // then
      then(thrown).isInstanceOf(OsduUnauthorizedException.class);

      InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
          fileLocationRepository, storageService);
      inOrder.verify(headersMapper).toHeaders(headers);
      inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowExceptionWhenFileLocationRequestIsInvalid() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(FILE_ID)
          .build();
      MessageHeaders headers = getMessageHeaders();

      willThrow(ConstraintViolationException.class).given(validationService)
          .validateFileLocationRequest(request);

      // when
      Throwable thrown = catchThrowable(() -> locationService.getFileLocation(request, headers));

      // then
      then(thrown).isInstanceOf(ConstraintViolationException.class);

      InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
          fileLocationRepository, storageService);
      inOrder.verify(headersMapper).toHeaders(headers);
      inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verify(validationService).validateFileLocationRequest(request);
      inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowExceptionWhenFileLocationIsNotFound() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(FILE_ID)
          .build();
      MessageHeaders headers = getMessageHeaders();

      // when
      Throwable thrown = catchThrowable(() -> locationService.getFileLocation(request, headers));

      // then
      then(thrown)
          .isInstanceOf(FileLocationNotFoundException.class)
          .hasMessage("Not found location for fileID : " + FILE_ID);

      InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
          fileLocationRepository, storageService);
      inOrder.verify(headersMapper).toHeaders(headers);
      inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
      inOrder.verify(validationService).validateFileLocationRequest(request);
      inOrder.verify(fileLocationRepository).findByFileID(FILE_ID);
      inOrder.verifyNoMoreInteractions();
    }

  }

  private MessageHeaders getMessageHeaders() {
    Map<String, Object> headers = new HashMap<>();
    headers.put(Headers.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.put(Headers.PARTITION, PARTITION);

    return new MessageHeaders(headers);
  }

  private SignedUrl getSignedUrl() {
    String bucketName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);
    String filename = TestUtils.getUuidString();

    URI uri = TestUtils.getObjectUri(bucketName, folderName, filename);
    URL url = TestUtils.getObjectUrl(bucketName, folderName, filename);

    return SignedUrl.builder()
        .uri(uri)
        .url(url)
        .createdAt(TestUtils.now())
        .createdBy(USER_DES_ID)
        .build();
  }

  private FileLocation getFileLocation() {
    String bucketName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);
    String filename = TestUtils.getUuidString();

    String uri = String
        .format("%s%s/%s/%s", SRG_PROTOCOL, bucketName, folderName, filename);
    return FileLocation.builder()
        .driver(DriverType.GCS)
        .location(uri)
        .build();
  }

  private static LocationResponse getLocationResponseAnswer(InvocationOnMock invoc) {
    SignedUrl signedUrl = invoc.getArgument(0, SignedUrl.class);
    FileLocation fileLocation = invoc.getArgument(1, FileLocation.class);

    Map<String, String> location = new HashMap<>();
    location.put(SIGNED_URL_KEY, signedUrl.getUrl().toString());
    return LocationResponse.builder()
        .fileID(fileLocation.getFileID())
        .location(location)
        .build();
  }

}
