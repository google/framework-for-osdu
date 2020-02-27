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

import java.util.Date;
import org.opengroup.osdu.file.provider.gcp.model.entity.FileLocationEntity;
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository;
import org.springframework.cloud.gcp.data.datastore.repository.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface FileLocationEntityRepository
    extends DatastoreRepository<FileLocationEntity, Long> {

  @Nullable
  FileLocationEntity findByFileID(String fileID);

  @Query("SELECT * FROM `file-locations`"
      + " WHERE CreatedAt >= @time_from AND CreatedAt <= @time_to AND CreatedBy = @user_id")
  Page<FileLocationEntity> findFileList(@Param("time_from") Date from, @Param("time_to") Date to,
      @Param("user_id") String userID, Pageable pageable);

}
