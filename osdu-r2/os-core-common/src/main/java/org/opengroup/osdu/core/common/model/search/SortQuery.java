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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.SwaggerDoc;

import java.util.List;

@Data
@NoArgsConstructor
public class SortQuery {

    @ApiModelProperty(value = SwaggerDoc.SORT_FIELD_DESCRIPTION, dataType = "[Ljava.lang.String;")
    private List<String> field;

    @ApiModelProperty(value = SwaggerDoc.SORT_ORDER_DESCRIPTION, dataType = "[Lorg.opengroup.osdu.search.model.SortOrder;")
    private List<SortOrder> order;

    public String getFieldByIndex(int index) {
        return field.get(index);
    }

    public SortOrder getOrderByIndex(int index) {
        return order.get(index);
    }
}

