package com.osdu.model.type.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Coordinate {

  @JsonProperty("x")
  Double x;

  @JsonProperty("y")
  Double y;

}
