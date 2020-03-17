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

package org.opengroup.osdu.core.common.model.crs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrajectoryOutputStation {

    @JsonProperty
    private double md;
    @JsonProperty
    private double inclination;
    @JsonProperty
    private double azimuthTN;
    @JsonProperty
    private double azimuthGN;
    @JsonProperty
    private double dxTN;
    @JsonProperty
    private double dyTN;
    @JsonProperty
    private Point point;
    @JsonProperty
    private double wgs84Longitude;
    @JsonProperty
    private double wgs84Latitude;
    @JsonProperty
    private double dz;
    @JsonProperty
    private double dls;
    @JsonProperty
    private boolean original;

    public TrajectoryOutputStation() {
    }
}
