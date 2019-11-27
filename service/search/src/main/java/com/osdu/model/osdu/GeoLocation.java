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

import java.util.List;
import lombok.Data;

/**
 * Wrapper class for OSDU GeoLocation object. coordinates are an object array due to different
 * possible variations of incoming object - for point it can be just an array of two doubles, but
 * for polygon it will be an array of arrays of doubles located in the same property of the JSON
 * file.
 */
@Data
public class GeoLocation {

  Double distance;
  String type;
  List<List<Double>> coordinates;

}
