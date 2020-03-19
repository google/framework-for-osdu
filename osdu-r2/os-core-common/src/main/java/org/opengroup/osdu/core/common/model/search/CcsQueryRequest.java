// Copyright 2017-2019, Schlumberger
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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.opengroup.osdu.core.common.SwaggerDoc;
import org.opengroup.osdu.core.common.model.search.validation.CcsValidOffset;
import org.opengroup.osdu.core.common.model.search.validation.ValidMultiKind;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Data
@Validated
@NoArgsConstructor
@CcsValidOffset
public class CcsQueryRequest {

    @Min(value = 0, message = SwaggerDoc.OFFSET_VALIDATION_MIN_MSG)
    @JsonProperty("offset")
    @ApiModelProperty(value = SwaggerDoc.OFFSET_DESCRIPTION, dataType = "java.lang.Integer", example = "0")
    private int from;

    @NotBlank(message = SwaggerDoc.KIND_VALIDATION_CAN_NOT_BE_NULL_OR_EMPTY)
    @ApiModelProperty(value = SwaggerDoc.KIND_REQUEST_DESCRIPTION, required = true, example = SwaggerDoc.KIND_EXAMPLE)
    @ValidMultiKind
    private String kind;

    @Min(value = 0, message = SwaggerDoc.LIMIT_VALIDATION_MIN_MSG)
    @ApiModelProperty(value = SwaggerDoc.LIMIT_DESCRIPTION, dataType = "java.lang.Integer", example = "30")
    private int limit;

    @ApiModelProperty(value = SwaggerDoc.QUERY_DESCRIPTION)
    private String query = "";

    @ApiModelProperty(value = SwaggerDoc.QUERYASOWNER_DESCRIPTION, dataType = "java.lang.Boolean", example = "false")
    private boolean queryAsOwner;

    @Override
    public String toString(){
        return new com.google.gson.Gson().toJson(this);
    }
}
