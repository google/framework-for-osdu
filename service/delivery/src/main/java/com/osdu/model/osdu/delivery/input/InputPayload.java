package com.osdu.model.osdu.delivery.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class InputPayload {

    List<String> srns;

    String regionId;

    @JsonCreator
    public InputPayload(@JsonProperty(value = "SRNS", required = true) List<String> srns,
                        @JsonProperty(value = "TargetRegionID", required = true) String regionId) {
        this.srns = srns;
        this.regionId = regionId;
    }
}
