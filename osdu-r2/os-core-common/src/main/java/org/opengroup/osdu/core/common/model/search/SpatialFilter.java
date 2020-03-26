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
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.opengroup.osdu.core.common.SwaggerDoc;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class SpatialFilter {

    @NotBlank(message = SwaggerDoc.FIELD_VALIDATION_NON_NULL_MSG)
    @ApiModelProperty(value = SwaggerDoc.FIELD_DESCRIPTION)
    private String field;

    @Valid
    @ApiModelProperty(value = SwaggerDoc.QUERY_BY_BOUNDING_BOX_DESCRIPTION)
    private ByBoundingBox byBoundingBox;

    @Valid
    @ApiModelProperty(value = SwaggerDoc.QUERY_BY_DISTANCE_DESCRIPTION)
    private ByDistance byDistance;

    @Valid
    @ApiModelProperty(value = SwaggerDoc.QUERY_BY_GEO_POLYGON_DESCRIPTION)
    private ByGeoPolygon byGeoPolygon;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ByBoundingBox {

        @NotNull(message = SwaggerDoc.TOPLEFT_VALIDATION_NON_NULL_MSG)
        @Valid
        @ApiModelProperty(value = SwaggerDoc.TOPLEFT_BOUNDING_BOX_DESCRIPTION)
        private Coordinate topLeft;

        @NotNull(message = SwaggerDoc.BOTTOMRIGHT_VALIDATION_NON_NULL_MSG)
        @Valid
        @ApiModelProperty(value = SwaggerDoc.BOTTOMRIGHT_BOUNDING_BOX_DESCRIPTION)
        private Coordinate bottomRight;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ByDistance {

        @Min(value = 0, message = SwaggerDoc.DISTANCE_VALIDATION_MIN_MSG)
        @Max(value = (long) Double.MAX_VALUE, message = SwaggerDoc.DISTANCE_VALIDATION_MAX_MSG)
        @ApiModelProperty(value = SwaggerDoc.DISTANCE_DESCRIPTION, dataType = "java.lang.Double", example = "1500")
        private double distance;

        @NotNull(message = SwaggerDoc.DISTANCE_POINT_VALIDATION_NON_NULL_MSG)
        @Valid
        @ApiModelProperty(value = SwaggerDoc.POINT_DISTANCE_DESCRIPTION)
        private Coordinate coordinate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ByGeoPolygon {

        @NotEmpty(message = SwaggerDoc.GEOPOLYGON_POINT_VALIDATION_NON_NULL_MSG)
        @Valid
        @ApiModelProperty(value = SwaggerDoc.POINTS_GEO_POLYGON_DESCRIPTION)
        private List<Coordinate> coordinates;
    }
}
