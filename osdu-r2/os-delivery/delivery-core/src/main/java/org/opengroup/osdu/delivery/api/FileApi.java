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

package org.opengroup.osdu.delivery.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileRequest;
import org.opengroup.osdu.core.common.model.file.FileResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.delivery.provider.interfaces.FileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
public class FileApi {

  final DpsHeaders headers;
  final FileService fileService;

  // TODO: Create the permission for os-delivery and change pre authorize annotation
  @PostMapping("/getFile")
  @PreAuthorize("@authorizationFilter.hasPermission('" + StorageRole.CREATOR + "')")
  public FileResponse getFile(@RequestBody FileRequest request) {
    log.debug("Get file request received, with following parameters: {}", request);
    FileResponse fileResponse = fileService.getFile(request, headers);
    log.debug("Get file result ready : {}", fileResponse);
    return fileResponse;
  }
}
