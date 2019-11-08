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

package com.osdu.model.osdu.delivery.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

@Data
public class InputPayload {

  @JsonProperty(value = "SRNS")
  List<String> srns;

  @JsonProperty(value = "TargetRegionID")
  String regionId;

  @JsonCreator
  public InputPayload(@JsonProperty(value = "SRNS", required = true) List<String> srns,
      @JsonProperty(value = "TargetRegionID", required = true) String regionId) {
    this.srns = srns;
    this.regionId = regionId;
  }
}
