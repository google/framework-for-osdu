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

package org.opengroup.osdu.file.provider.gcp.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.model.file.FileLocation.Fields;
import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity;
import org.springframework.cloud.gcp.data.datastore.core.mapping.Field;
import org.springframework.data.annotation.Id;

@Entity(name = "file-locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileLocationEntity {

  @Id
  Long id;

  @Field(name = Fields.FILE_ID)
  String fileID;

  @Field(name = Fields.DRIVER)
  String driver;

  @Field(name = Fields.LOCATION)
  String location;

  @Field(name = Fields.CREATED_AT)
  Date createdAt;

  @Field(name = Fields.CREATED_BY)
  String createdBy;

}
