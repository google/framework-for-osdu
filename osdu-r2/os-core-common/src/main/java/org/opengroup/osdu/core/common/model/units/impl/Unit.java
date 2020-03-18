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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URLDecoder;

@JsonIgnoreProperties({"valid", "angle", "length", "offset", "ancestry"})
@Data
@EqualsAndHashCode(callSuper = false)
public class Unit extends UnitParameters {

    public double offset;
    @JsonProperty("ScaleOffset")
    protected ScaleOffset scaleOffset;
    @JsonProperty("ABCD")
    protected Abcd abcd;
    @JsonProperty("Symbol")
    protected String symbol;

    @JsonProperty("BaseMeasurement")
    protected String baseMeasurement;

    protected String ancestry;

    public static Unit createInstance(String unitReference) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

            String decoded = URLDecoder.decode(unitReference, "UTF-8");
            JsonNode node = mapper.readTree(decoded);
            return mapper.treeToValue(node, Unit.class);
        } catch (Exception e) {
            return new Unit(); // return an empty, invalid unit
        }
    }

    public String getAncestry() {

        if (this.ancestry == null && this.baseMeasurement != null) {
            try {
                String decoded = URLDecoder.decode(this.baseMeasurement, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
                JsonNode node = mapper.readTree(decoded);
                Measurement measurement = mapper.treeToValue(node, Measurement.class);
                if (measurement != null) {
                    this.ancestry = measurement.getAncestry();
                }
            } catch (Exception e) {
                return null;
            }
        }
        return ancestry;
    }

    public boolean isValid() {
        boolean valid = this.abcd != null || this.scaleOffset != null;
        return valid && this.getAncestry() != null;
    }

    public double scaleToSI() {
        if (isValid()) {
            if (this.abcd != null) return this.abcd.scaleToSI();
            else if (this.scaleOffset != null) return this.scaleOffset.scaleToSI();
        }
        return Double.NaN;
    }

    public double getOffset() {
        if (isValid()) {
            if (this.abcd != null) return -this.abcd.getA() / this.getAbcd().getB();
            else if (this.scaleOffset != null) return this.scaleOffset.getOffset();
        }
        return Double.NaN;
    }
}

