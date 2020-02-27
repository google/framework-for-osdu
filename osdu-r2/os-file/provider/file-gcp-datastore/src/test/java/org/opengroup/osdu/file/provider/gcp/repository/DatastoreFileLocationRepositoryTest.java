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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FileLocation.Fields;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.exception.FileLocationNotFoundException;
import org.opengroup.osdu.file.provider.gcp.TestUtils;
import org.opengroup.osdu.file.provider.gcp.mapper.FileLocationMapper;
import org.opengroup.osdu.file.provider.gcp.model.entity.FileLocationEntity;
import org.opengroup.osdu.file.provider.interfaces.FileLocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class DatastoreFileLocationRepositoryTest {

  private static final String GCS_LOCATION = "gs://bucket/folder/file.tmp";
  private static final String TEMP_USER = "temp-user";

  @Spy
  private FileLocationMapper fileLocationMapper = Mappers.getMapper(FileLocationMapper.class);
  @Mock
  private FileLocationEntityRepository fileLocationEntityRepository;

  @Captor
  private ArgumentCaptor<FileLocationEntity> entityCaptor;

  private FileLocationRepository fileLocationRepository;

  @BeforeEach
  void setUp() {
    fileLocationRepository
        = new DatastoreFileLocationRepository(fileLocationMapper, fileLocationEntityRepository);
  }

  @Test
  void shouldFindFileLocationEntityByFileId() {
    // given
    Date createdDate = new Date();
    long entityId = RandomUtils.nextLong();
    given(fileLocationEntityRepository.findByFileID(TestUtils.FILE_ID))
        .willReturn(getFileLocationEntity(entityId, createdDate));

    // when
    FileLocation fileLocation = fileLocationRepository.findByFileID(TestUtils.FILE_ID);

    // then
    verify(fileLocationEntityRepository).findByFileID(TestUtils.FILE_ID);
    then(fileLocation).isEqualTo(getFileLocation(createdDate));
  }

  @Test
  void shouldSaveFileLocationAndReturnSavedEntity() {
    // given
    Date createdDate = new Date();
    long entityId = RandomUtils.nextLong();

    given(fileLocationEntityRepository.save(any(FileLocationEntity.class)))
        .willReturn(getFileLocationEntity(entityId, createdDate));

    // when
    FileLocation fileLocation = fileLocationRepository.save(getFileLocation(createdDate));

    // then
    verify(fileLocationEntityRepository).save(entityCaptor.capture());
    then(fileLocation).isEqualTo(getFileLocation(createdDate));
    then(entityCaptor.getValue()).isEqualTo(getFileLocationEntity(null, createdDate));
  }

  @Test
  void shouldReturnFirstPageWith2Element() {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime timeFrom = now.minusHours(1);
    FileListRequest request = FileListRequest.builder()
        .timeFrom(timeFrom)
        .timeTo(now)
        .pageNum(1)
        .items((short) 5)
        .userID(TEMP_USER)
        .build();
    PageRequest pageable = PageRequest.of(1, 5, Direction.ASC, Fields.CREATED_AT);

    given(fileLocationEntityRepository.findFileList(toDate(timeFrom), toDate(now), TEMP_USER,
        pageable)).willReturn(getPage(2, pageable, 42));

    // when
    FileListResponse fileListResponse = fileLocationRepository.findAll(request);

    // then
    then(fileListResponse).isEqualToIgnoringGivenFields(FileListResponse.builder()
        .number(1)
        .numberOfElements(2)
        .size(5)
        .build(), "content");
    then(fileListResponse.getContent()).hasSize(2);
  }

  @Test
  void shouldThrowExceptionWhenNothingFoundByPageQuery() {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime timeFrom = now.minusHours(1);
    FileListRequest request = FileListRequest.builder()
        .timeFrom(now.minusHours(1))
        .timeTo(now)
        .pageNum(42)
        .items((short) 7)
        .userID(TEMP_USER)
        .build();
    PageRequest pageable = PageRequest.of(42, 7, Direction.ASC, Fields.CREATED_AT);

    given(fileLocationEntityRepository.findFileList(toDate(timeFrom), toDate(now), TEMP_USER,
        pageable)).willReturn(getPage(0, pageable, 42));

    // when
    Throwable thrown = catchThrowable(() -> fileLocationRepository.findAll(request));

    // then
    then(thrown)
        .isInstanceOf(FileLocationNotFoundException.class)
        .hasMessage("Nothing found for such filter and page(num: 42, size: 7).");
  }

  private FileLocation getFileLocation(Date createdDate) {
    return FileLocation.builder()
        .fileID(TestUtils.FILE_ID)
        .driver(DriverType.GCS)
        .location(GCS_LOCATION)
        .createdBy(TEMP_USER)
        .createdAt(createdDate)
        .build();
  }

  private FileLocationEntity getFileLocationEntity(Long id, Date createdDate) {
    return FileLocationEntity.builder()
        .id(id)
        .fileID(TestUtils.FILE_ID)
        .driver(DriverType.GCS.name())
        .location(GCS_LOCATION)
        .createdBy(TEMP_USER)
        .createdAt(createdDate)
        .build();
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.toInstant(ZoneOffset.UTC));
  }

  private Page<FileLocationEntity> getPage(int size,  PageRequest pageable, long total) {
    List<FileLocationEntity> entities = IntStream.range(0, size)
        .mapToObj(i -> getFileLocationEntity(null, null))
        .collect(Collectors.toList());
    return new PageImpl<>(entities, pageable, total);
  }

}
