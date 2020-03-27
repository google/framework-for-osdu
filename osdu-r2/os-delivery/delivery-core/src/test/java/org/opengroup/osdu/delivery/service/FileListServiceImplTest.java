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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.opengroup.osdu.delivery.TestUtils.AUTHORIZATION_TOKEN;
import static org.opengroup.osdu.delivery.TestUtils.PARTITION;

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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.delivery.provider.interfaces.FileListService;
import org.opengroup.osdu.delivery.provider.interfaces.FileLocationRepository;
import org.opengroup.osdu.delivery.provider.interfaces.ValidationService;

@ExtendWith(MockitoExtension.class)
class FileListServiceImplTest {

  private static final String GCS_FOLDER = "gs://bucket/folder/";
  private static final String TEMP_USER = "temp-user";

  @Mock
  private ValidationService validationService;
  @Mock
  private FileLocationRepository fileLocationRepository;

  private FileListService fileListService;

  @BeforeEach
  void setUp() {
    fileListService = new FileListServiceImpl(validationService, fileLocationRepository);
  }

  @Test
  void shouldReturnFileListByRequest() {
    // given
    LocalDateTime now = LocalDateTime.now();
    FileListRequest request = FileListRequest.builder()
        .timeFrom(now.minusHours(1))
        .timeTo(now)
        .pageNum(0)
        .items((short) 5)
        .userID(TEMP_USER)
        .build();
    DpsHeaders headers = getHeaders();

    given(fileLocationRepository.findAll(request)).willReturn(FileListResponse.builder()
        .content(Arrays.asList(
            getFileLocation(toDate(now.minusMinutes(10))),
            getFileLocation(toDate(now.minusMinutes(20)))))
        .number(0)
        .numberOfElements(2)
        .size(5)
        .build());

    // when
    FileListResponse response = fileListService.getFileList(request, headers);

    // then
    then(response).isEqualToIgnoringGivenFields(FileListResponse.builder()
        .number(0)
        .numberOfElements(2)
        .size(5)
        .build(), "content");
    then(response.getContent()).hasSize(2);

    InOrder inOrder = Mockito.inOrder(validationService, fileLocationRepository);
    inOrder.verify(validationService).validateFileListRequest(request);
    inOrder.verify(fileLocationRepository).findAll(request);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowExceptionWhenGetFileListRequestIsInvalid() {
    // given
    FileListRequest request = FileListRequest.builder()
        .build();
    DpsHeaders headers = getHeaders();

    willThrow(ConstraintViolationException.class).given(validationService)
        .validateFileListRequest(request);

    // when
    Throwable thrown = catchThrowable(() -> fileListService.getFileList(request, headers));

    // then
    then(thrown).isInstanceOf(ConstraintViolationException.class);

    InOrder inOrder = Mockito.inOrder(validationService, fileLocationRepository);
    inOrder.verify(validationService).validateFileListRequest(request);
    inOrder.verifyNoMoreInteractions();
  }

  private DpsHeaders getHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put(DpsHeaders.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.put(DpsHeaders.DATA_PARTITION_ID, PARTITION);

    return DpsHeaders.createFromMap(headers);
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
