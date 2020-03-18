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

package org.opengroup.osdu.core.common.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.SwaggerDoc;
import org.opengroup.osdu.core.common.model.search.validation.ValidOffset;

import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidOffset
public class QueryRequest extends Query {

    @Min(value = 0, message = SwaggerDoc.OFFSET_VALIDATION_MIN_MSG)
    @JsonProperty("offset")
    @ApiModelProperty(value = SwaggerDoc.OFFSET_DESCRIPTION, dataType = "java.lang.Integer", example = "0")
    private int from;

    // aggregation: only make it available in pre demo for now
    @ApiModelProperty(value = SwaggerDoc.AGGREGATEBY_DESCRIPTION, dataType = "java.lang.String", hidden = true)
    private String aggregateBy;

    @Override
    public String toString(){
        return new com.google.gson.Gson().toJson(this);
    }
}
