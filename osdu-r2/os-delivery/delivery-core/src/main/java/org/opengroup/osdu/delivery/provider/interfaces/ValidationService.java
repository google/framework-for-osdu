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

package org.opengroup.osdu.delivery.provider.interfaces;

import javax.validation.ConstraintViolationException;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.LocationRequest;

public interface ValidationService {

  /**
   * Validates location request using Java Bean Validation.
   *
   * @param request location request
   * @throws ConstraintViolationException if request is invalid
   */
  void validateLocationRequest(LocationRequest request);

  /**
   * Validates file location request using Java Bean Validation.
   *
   * @param request location request
   * @throws ConstraintViolationException if request is invalid
   */
  void validateFileLocationRequest(FileLocationRequest request);

  /**
   * Validates file list request using Java Bean Validation.
   *
   * @param request location request
   * @throws ConstraintViolationException if request is invalid
   */
  void validateFileListRequest(FileListRequest request);

}
