/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.ingest.validation.Extended;
import org.opengroup.osdu.ingest.validation.ValidSubmitRequest;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ValidSubmitRequest(groups = Extended.class)
public class SubmitRequest {

  @JsonProperty("FileID")
  String fileId;

  @JsonProperty("DataType")
  DataType dataType;

}
