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

package org.opengroup.osdu.file.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.opengroup.osdu.file.TestUtils.AUTHORIZATION_TOKEN;
import static org.opengroup.osdu.file.TestUtils.PARTITION;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FilesListRequest;
import org.opengroup.osdu.core.common.model.file.FilesListResponse;
import org.opengroup.osdu.file.exception.OsduUnauthorizedException;
import org.opengroup.osdu.file.mapper.HeadersMapper;
import org.opengroup.osdu.file.model.Headers;
import org.opengroup.osdu.file.repository.FileLocationRepository;
import org.opengroup.osdu.file.validation.ValidationService;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
class FilesListServiceImplTest {

  private static final String GCS_FOLDER = "gs://bucket/folder/";
  private static final String TEMP_USER = "temp-user";

  @Spy
  private HeadersMapper headersMapper = Mappers.getMapper(HeadersMapper.class);
  @Mock
  private AuthenticationService authenticationService;
  @Mock
  private ValidationService validationService;
  @Mock
  private FileLocationRepository fileLocationRepository;

  private FilesListService filesListService;

  @BeforeEach
  void setUp() {
    filesListService = new FilesListServiceImpl(headersMapper, authenticationService,
        validationService, fileLocationRepository);
  }

  @Test
  void shouldReturnFilesListByRequest() {
    // given
    LocalDateTime now = LocalDateTime.now();
    FilesListRequest request = FilesListRequest.builder()
        .timeFrom(now.minusHours(1))
        .timeTo(now)
        .pageNum(0)
        .items((short) 5)
        .userID(TEMP_USER)
        .build();
    MessageHeaders headers = getMessageHeaders();

    given(fileLocationRepository.findAll(request)).willReturn(FilesListResponse.builder()
        .content(Arrays.asList(
            getFileLocation(toDate(now.minusMinutes(10))),
            getFileLocation(toDate(now.minusMinutes(20)))))
        .number(0)
        .numberOfElements(2)
        .size(5)
        .build());

    // when
    FilesListResponse response = filesListService.getFilesList(request, headers);

    // then
    then(response).isEqualToIgnoringGivenFields(FilesListResponse.builder()
        .number(0)
        .numberOfElements(2)
        .size(5)
        .build(), "content");
    then(response.getContent()).hasSize(2);

    InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
        fileLocationRepository);
    inOrder.verify(headersMapper).toHeaders(headers);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(validationService).validateFilesListRequest(request);
    inOrder.verify(fileLocationRepository).findAll(request);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowExceptionForGetFilesListWhenCheckingAuthenticationIsFailed() {
    // given
    FilesListRequest request = FilesListRequest.builder()
        .build();
    MessageHeaders headers = getMessageHeaders();

    willThrow(OsduUnauthorizedException.class).given(authenticationService)
        .checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);

    // when
    Throwable thrown = catchThrowable(() -> filesListService.getFilesList(request, headers));

    // then
    then(thrown).isInstanceOf(OsduUnauthorizedException.class);

    InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
        fileLocationRepository);
    inOrder.verify(headersMapper).toHeaders(headers);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowExceptionWhenGetFilesListRequestIsInvalid() {
    // given
    FilesListRequest request = FilesListRequest.builder()
        .build();
    MessageHeaders headers = getMessageHeaders();

    willThrow(ConstraintViolationException.class).given(validationService)
        .validateFilesListRequest(request);

    // when
    Throwable thrown = catchThrowable(() -> filesListService.getFilesList(request, headers));

    // then
    then(thrown).isInstanceOf(ConstraintViolationException.class);

    InOrder inOrder = Mockito.inOrder(headersMapper, authenticationService, validationService,
        fileLocationRepository);
    inOrder.verify(headersMapper).toHeaders(headers);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(validationService).validateFilesListRequest(request);
    inOrder.verifyNoMoreInteractions();
  }

  private MessageHeaders getMessageHeaders() {
    Map<String, Object> headers = new HashMap<>();
    headers.put(Headers.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.put(Headers.PARTITION, PARTITION);

    return new MessageHeaders(headers);
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.toInstant(ZoneOffset.UTC));
  }

  private FileLocation getFileLocation(Date createdDate) {
    String fileID = RandomStringUtils.randomAlphanumeric(3, 32);
    return FileLocation.builder()
        .fileID(fileID)
        .driver(DriverType.GCS)
        .location(GCS_FOLDER + fileID)
        .createdAt(createdDate)
        .createdBy(TEMP_USER)
        .build();
  }

}
