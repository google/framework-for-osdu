// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.model.search;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.SwaggerDoc;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {

    @Max(value = 90, message = SwaggerDoc.LATITUDE_VALIDATION_RANGE_MSG)
    @Min(value = -90, message = SwaggerDoc.LATITUDE_VALIDATION_RANGE_MSG)
    @ApiModelProperty(value = SwaggerDoc.LATITUDE, dataType = "java.lang.Double", example = "37.450727")
    private double latitude;

    @Max(value = 180, message = SwaggerDoc.LONGITUDE_VALIDATION_RANGE_MSG)
    @Min(value = -180, message = SwaggerDoc.LONGITUDE_VALIDATION_RANGE_MSG)
    @ApiModelProperty(value = SwaggerDoc.LONGITUDE, dataType = "java.lang.Double", example = "-122.174762")
    private double longitude;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate coordinate = (Coordinate) o;

        if (Double.compare(coordinate.latitude, latitude) != 0) return false;
        return Double.compare(coordinate.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = latitude != +0.0d ? Double.doubleToLongBits(latitude) : 0L;
        result = Long.hashCode(temp);
        temp = longitude != +0.0d ? Double.doubleToLongBits(longitude) : 0L;
        result = 31 * result + Long.hashCode(temp);
        return result;
    }
}
