package com.osdu.model.osdu.delivery.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

@Data
public class InputPayload {

  @JsonProperty(value = "SRNS")
  List<String> srns;

  @JsonProperty(value = "TargetRegionID")
  String regionId;

  @JsonCreator
  public InputPayload(@JsonProperty(value = "SRNS", required = true) List<String> srns,
      @JsonProperty(value = "TargetRegionID", required = true) String regionId) {
    this.srns = srns;
    this.regionId = regionId;
  }
}
