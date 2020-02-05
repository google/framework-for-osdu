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

package org.opengroup.osdu.core.common.model.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileListResponse {

  /**
   * The page content.
   */
  @JsonProperty("Content")
  List<FileLocation> content;

  /**
   * The number of current page.
   */
  @JsonProperty("Number")
  int number;

  /**
   * The number of elements currently on this page.
   */
  @JsonProperty("NumberOfElements")
  int numberOfElements;

  /**
   * The size of the page.
   */
  @JsonProperty("Size")
  int size;

}
