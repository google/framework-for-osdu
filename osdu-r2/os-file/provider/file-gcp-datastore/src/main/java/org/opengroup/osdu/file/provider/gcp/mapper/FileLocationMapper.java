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

package org.opengroup.osdu.file.provider.gcp.mapper;

import org.mapstruct.Mapper;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FileLocation.FileLocationBuilder;
import org.opengroup.osdu.file.provider.gcp.model.entity.FileLocationEntity;

@Mapper
public abstract class FileLocationMapper {

  /**
   * Map file location Datastore entity to file location model.
   *
   * @param entity file location entity
   * @return file location
   */
  public FileLocation toFileLocation(FileLocationEntity entity) {
    if (entity == null) {
      return null;
    }

    FileLocationBuilder fileLocationBuilder = FileLocation.builder();

    fileLocationBuilder.fileID(entity.getFileID());
    if (entity.getDriver() != null) {
      fileLocationBuilder.driver(DriverType.valueOf(entity.getDriver()));
    }
    fileLocationBuilder.location(entity.getLocation());
    fileLocationBuilder.createdAt(entity.getCreatedAt());
    fileLocationBuilder.createdBy(entity.getCreatedBy());

    return fileLocationBuilder
        .build();
  }

  /**
   * Map file location model to file location Datastore entity.
   *
   * @param fileLocation file location
   * @return file location Datastore entity
   */
  public abstract FileLocationEntity toEntity(FileLocation fileLocation);

}
