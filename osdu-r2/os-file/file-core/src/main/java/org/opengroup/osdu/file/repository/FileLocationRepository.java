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

package org.opengroup.osdu.file.repository;

import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FilesListRequest;
import org.opengroup.osdu.core.common.model.file.FilesListResponse;

public interface FileLocationRepository {

  /**
   * Finds a file location by file ID in a collection.
   *
   * @param fileID file ID
   * @return file location if it's found otherwise null
   */
  FileLocation findByFileID(String fileID);

  /**
   * Saves a file location in a collection.
   *
   * @param fileLocation file location
   * @return saved file location with populated ID
   */
  FileLocation save(FileLocation fileLocation);

  /**
   * Finds a files list page by request.
   *
   * @param request request
   * @return files list page
   */
  FilesListResponse findAll(FilesListRequest request);

}
