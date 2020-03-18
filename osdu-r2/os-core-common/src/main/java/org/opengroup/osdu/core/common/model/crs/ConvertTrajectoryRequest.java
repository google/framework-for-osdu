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

package org.opengroup.osdu.core.common.model.crs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.util.List;

@Data
@AllArgsConstructor
public class ConvertTrajectoryRequest {
    @JsonProperty("trajectoryCRS")
    private String trajectoryCRS;
    @JsonProperty("azimuthReference")
    private String azimuthReference;
    @JsonProperty("unitXY")
    private String unitXY;
    @JsonProperty("unitZ")
    private String unitZ;
    @JsonProperty("referencePoint")
    private Point referencePoint;
    @JsonProperty("inputStations")
    private List<TrajectoryInputStation> inputStations;
    @JsonProperty("method")
    private String method;
    @JsonProperty("inputKind")
    private String inputKind;
    @JsonProperty("interpolate")
    private boolean interpolate;

    public ConvertTrajectoryRequest() {
        interpolate = true;
        inputKind = TrajectoryInputKind.MD_INCL_AZIM.toString();
    }

    public static ConvertTrajectoryRequest createInstance(String json) {
        ConvertTrajectoryRequest result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, ConvertTrajectoryRequest.class);

        } catch (IOException e) {
            return result;
        }
        return result;
    }

    @Override
    public String toString() {
        String result = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            result = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return result;
        }
        return result;
    }
}
