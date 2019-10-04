package com.osdu.model.osdu.delivery.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseRecord;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class ResponseItem {

  @JsonProperty("FileLocation")
  String fileLocation;

  @JsonProperty("Data")
  BaseRecord data;

  @JsonProperty("SRN")
  String srn;

}
