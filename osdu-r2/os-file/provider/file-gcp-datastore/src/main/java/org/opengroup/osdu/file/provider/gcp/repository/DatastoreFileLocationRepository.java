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

import static java.lang.String.format;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FileLocation.Fields;
import org.opengroup.osdu.file.exception.FileLocationNotFoundException;
import org.opengroup.osdu.file.provider.gcp.mapper.FileLocationMapper;
import org.opengroup.osdu.file.provider.gcp.model.entity.FileLocationEntity;
import org.opengroup.osdu.file.provider.interfaces.FileLocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DatastoreFileLocationRepository implements FileLocationRepository {

  final FileLocationMapper fileLocationMapper;
  final FileLocationEntityRepository fileLocationEntityRepository;

  @Override
  public FileLocation findByFileID(String fileID) {
    log.debug("Requesting file location. File ID : {}", fileID);
    FileLocationEntity entity = fileLocationEntityRepository.findByFileID(fileID);
    log.debug("Found file location : {}", entity);
    return fileLocationMapper.toFileLocation(entity);
  }

  @Override
  public FileLocation save(FileLocation fileLocation) {
    log.info("Saving file location : {}", fileLocation);
    FileLocationEntity saved = fileLocationEntityRepository
        .save(fileLocationMapper.toEntity(fileLocation));
    log.info("Fetch saved file location : {}", saved);
    return fileLocationMapper.toFileLocation(saved);
  }

  @Override
  public FileListResponse findAll(FileListRequest request) {
    int pageSize = request.getItems();
    int pageNum = request.getPageNum();

    Page<FileLocation> page = fileLocationEntityRepository.findFileList(
        toDate(request.getTimeFrom()),
        toDate(request.getTimeTo()),
        request.getUserID(),
        PageRequest.of(pageNum, pageSize, Direction.ASC, Fields.CREATED_AT))
        .map(fileLocationMapper::toFileLocation);

    if (page.isEmpty()) {
      throw new FileLocationNotFoundException(
          format("Nothing found for such filter and page(num: %s, size: %s).", pageNum, pageSize));
    }

    return FileListResponse.builder()
        .content(page.getContent())
        .size(page.getSize())
        .number(page.getNumber())
        .numberOfElements(page.getNumberOfElements())
        .build();
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.toInstant(ZoneOffset.UTC));
  }

}
