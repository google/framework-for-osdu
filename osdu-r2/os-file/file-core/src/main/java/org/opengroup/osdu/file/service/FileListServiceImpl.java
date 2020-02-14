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

package org.opengroup.osdu.file.service;

import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.file.mapper.HeadersMapper;
import org.opengroup.osdu.file.model.Headers;
import org.opengroup.osdu.file.provider.interfaces.AuthenticationService;
import org.opengroup.osdu.file.provider.interfaces.FileListService;
import org.opengroup.osdu.file.provider.interfaces.FileLocationRepository;
import org.opengroup.osdu.file.provider.interfaces.ValidationService;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileListServiceImpl implements FileListService {

  @Named
  final HeadersMapper headersMapper;
  final AuthenticationService authenticationService;
  final ValidationService validationService;
  final FileLocationRepository fileLocationRepository;

  @Override
  public FileListResponse getFileList(FileListRequest request, MessageHeaders messageHeaders) {
    log.debug("Request file list with parameters : {}, and headers, {}",
        request, messageHeaders);
    Headers headers = headersMapper.toHeaders(messageHeaders);

    authenticationService.checkAuthentication(headers.getAuthorizationToken(),
        headers.getPartitionID());
    validationService.validateFileListRequest(request);

    FileListResponse response = fileLocationRepository.findAll(request);
    log.debug("File list result : {}", response);
    return response;
  }

}
