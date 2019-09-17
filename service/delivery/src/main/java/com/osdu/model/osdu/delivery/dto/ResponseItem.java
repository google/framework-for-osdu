package com.osdu.model.osdu.delivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseItem {

  @JsonProperty("FileLocation")
  String fileLocation;

  @JsonProperty("Data")
  Object data;

  @JsonProperty("SRN")
  String srn;

}
