/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.core.common.model.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileLocation {

  @JsonProperty(Fields.FILE_ID)
  String fileID;

  @JsonProperty(Fields.DRIVER)
  DriverType driver;

  @JsonProperty(Fields.LOCATION)
  String location;

  @JsonProperty(Fields.CREATED_AT)
  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  Date createdAt;

  @JsonProperty(Fields.CREATED_BY)
  String createdBy;

  public static final class Fields {

    public static final String FILE_ID = "FileID";
    public static final String DRIVER = "Driver";
    public static final String LOCATION = "Location";
    public static final String CREATED_AT = "CreatedAt";
    public static final String CREATED_BY = "CreatedBy";

    private Fields() {
    }

  }
}
