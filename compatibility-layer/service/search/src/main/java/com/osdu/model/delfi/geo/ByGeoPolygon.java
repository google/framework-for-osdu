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

package com.osdu.model.delfi.geo;

import com.osdu.model.delfi.geo.exception.GeoLocationException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class ByGeoPolygon implements GeoLocation {

  List<Point> points;

  /**
   * Constructor.
   *
   * @param coordinates coordinates
   */
  public ByGeoPolygon(List<List<Double>> coordinates) {
    if (coordinates.size() < 3) {
      throw new GeoLocationException(String.format(
          "Polygon GeoJSON requires at least 3 points for creation, actual, received : %s ",
          coordinates.size()));
    }
    points = coordinates.stream()
        .map(GeoUtils::coordinatesToPoint).collect(Collectors.toList());
  }
}
