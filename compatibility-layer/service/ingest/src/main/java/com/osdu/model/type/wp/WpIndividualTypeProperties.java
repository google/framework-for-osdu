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

package com.osdu.model.type.wp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.type.base.IndividualTypeProperties;
import com.osdu.model.type.reference.LineageAssertion;
import com.osdu.model.type.reference.SpatialLocation;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class WpIndividualTypeProperties extends IndividualTypeProperties {

  @JsonProperty("Name")
  String name;

  @JsonProperty("Description")
  String description;

  @JsonProperty("CreationDateTime")
  LocalDateTime creationDateTime;

  @JsonProperty("Tags")
  List<String> tags;

  @JsonProperty("SpatialPoint")
  SpatialLocation spatialPoint;

  @JsonProperty("SpatialArea")
  SpatialLocation spatialArea;

  @JsonProperty("SubmitterName")
  String submitterName;

  @JsonProperty("BusinessActivites")
  List<String> businessActivites;

  @JsonProperty("AuthorIDs")
  List<String> authorIDs;

  @JsonProperty("LineageAssertions")
  List<LineageAssertion> lineageAssertions;

  @JsonProperty("Annotations")
  List<String> annotations;

  @Builder
  public WpIndividualTypeProperties(String name, String description,
      LocalDateTime creationDateTime, List<String> tags,
      SpatialLocation spatialPoint, SpatialLocation spatialArea, String submitterName,
      List<String> businessActivites, List<String> authorIDs,
      List<LineageAssertion> lineageAssertions, List<String> annotations) {
    super();
    this.name = name;
    this.description = description;
    this.creationDateTime = creationDateTime;
    this.tags = tags;
    this.spatialPoint = spatialPoint;
    this.spatialArea = spatialArea;
    this.submitterName = submitterName;
    this.businessActivites = businessActivites;
    this.authorIDs = authorIDs;
    this.lineageAssertions = lineageAssertions;
    this.annotations = annotations;
  }

}
