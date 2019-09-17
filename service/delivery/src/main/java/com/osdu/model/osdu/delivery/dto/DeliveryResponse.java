package com.osdu.model.osdu.delivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeliveryResponse {

  List<String> unprocessedSrns;

  @JsonProperty("Result")
  List<ResponseItem> result;

}
