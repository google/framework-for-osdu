/*
 * Copyright 2019 Google LLC
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

package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoadManifest extends BaseJsonObject {

  @JsonProperty(value = "WorkProduct")
  WorkProduct workProduct;

  @JsonProperty(value = "WorkProductComponents")
  List<WorkProductComponent> workProductComponents;

  @JsonProperty(value = "Files")
  List<ManifestFile> files;

}
