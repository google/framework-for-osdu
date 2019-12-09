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
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class ByDistance implements GeoLocation {

  private static final int Y_INDEX = 1;
  private static final int X_INDEX = 0;

  @NonNull
  Point point;
  @NonNull
  Double distance;

  /**
   * Constructor.
   *
   * @param coordinates coordinates
   * @param distance    distance
   */
  public ByDistance(List<List<Double>> coordinates, Double distance) {
    if (coordinates.size() != 1) {
      throw new GeoLocationException(
          " By Distance GeoJSON requires exactly 1 point for creation, actual, received "
              + coordinates.size());
    }
    List<Double> pointCoordinates = coordinates.get(0);
    this.point = new Point(Double.valueOf(pointCoordinates.get(X_INDEX).toString()),
        Double.valueOf(pointCoordinates.get(Y_INDEX).toString()));

    this.distance = distance;
  }
}
