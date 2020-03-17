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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryResponse {

    private List<Map<String, Object>> results = new ArrayList<>();
    private List<AggregationResponse> aggregations = new ArrayList<>();
    private long totalCount;

    @Override
    public String toString() {
        if (this.aggregations == null) {
            ExclusionStrategy strategy = new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes field) {
                    return "aggregations".equals(field.getName());
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            };
            return new GsonBuilder().addSerializationExclusionStrategy(strategy).create().toJson(this, QueryResponse.class);
        } else {
            return new Gson().toJson(this, QueryResponse.class);
        }
    }

    public static QueryResponse getEmptyResponse() {
        return QueryResponse.builder().results(Collections.emptyList()).aggregations(Collections.emptyList()).totalCount(0).build();
    }
}
