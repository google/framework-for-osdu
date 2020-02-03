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

import javax.validation.ConstraintViolationException;
import org.opengroup.osdu.core.common.model.file.FilesListRequest;
import org.opengroup.osdu.core.common.model.file.FilesListResponse;
import org.opengroup.osdu.file.exception.OsduUnauthorizedException;
import org.springframework.messaging.MessageHeaders;

public interface FilesListService {

  /**
   * GetFilesList will be used for audit purposes, since system relies on client to upload a file,
   * we should have an option to identify whether actual upload happened or not;
   * and potentially cleanup database.
   *
   * @param request location request
   * @param messageHeaders message headers
   * @return a paginated file location result.
   * @throws OsduUnauthorizedException if token and partitionID are missing or, invalid
   * @throws ConstraintViolationException if request is invalid
   */
  FilesListResponse getFilesList(FilesListRequest request, MessageHeaders messageHeaders);

}
