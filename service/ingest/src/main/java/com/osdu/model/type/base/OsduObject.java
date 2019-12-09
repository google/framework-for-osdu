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

package com.osdu.model.type.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OsduObject {

  @JsonProperty("ResourceID")
  String resourceID;

  @JsonProperty("ResourceTypeID")
  String resourceTypeID;

  @JsonProperty("ResourceHomeRegionID")
  String resourceHomeRegionID;

  @JsonProperty("ResourceHostRegionIDs")
  List<String> resourceHostRegionIDs;

  @JsonProperty("ResourceObjectCreationDateTime")
  LocalDateTime resourceObjectCreationDatetime;

  @JsonProperty("ResourceVersionCreationDateTime")
  LocalDateTime resourceVersionCreationDatetime;

  @JsonProperty("ResourceCurationStatus")
  String resourceCurationStatus;

  @JsonProperty("ResourceLifecycleStatus")
  String resourceLifecycleStatus;

  @JsonProperty("ResourceSecurityClassification")
  String resourceSecurityClassification;

  @JsonIgnore
  OsduObjectData data;

  @JsonProperty("Data")
  public OsduObjectData getData() {
    return data;
  }

}
