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

package com.osdu.model.osdu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.osdu.deserializer.MetadataDeserializer;
import com.osdu.model.SearchObject;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class OsduSearchObject implements SearchObject {

  Integer count;
  Integer start;
  String fulltext;
  @JsonDeserialize(using = MetadataDeserializer.class)
  Map<String, List<String>> metadata;
  List<String> facets;
  @JsonAlias("full-results")
  Boolean fullResults;
  SortOption[] sort;
  @JsonAlias("geo-location")
  GeoLocation geoLocation;
  @JsonAlias("geo-centroid")
  List<Double>[] geoCentroid;
}
