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

package org.opengroup.osdu.core.common.model.units.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;


@Data
@AllArgsConstructor
public class Abcd {
    @NonNull
    @JsonProperty("a")
    private Double a;
    @NonNull
    @JsonProperty("b")
    private Double b;
    @NonNull
    @JsonProperty("c")
    private Double c;
    @NonNull
    @JsonProperty("d")
    private Double d;

    public Abcd() {
        this.a = Double.NaN;
        this.b = Double.NaN;
        this.c = Double.NaN;
        this.d = Double.NaN;
    }

    public double scaleToSI() {
        return this.b / this.c;
    }

}
