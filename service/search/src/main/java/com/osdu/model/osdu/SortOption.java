package com.osdu.model.osdu;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.osdu.deserializer.SortOptionDeserializer;
import lombok.Data;

@Data
@JsonDeserialize(using = SortOptionDeserializer.class)
public class SortOption {

  String fieldName;
  OrderType orderType;
  public enum OrderType {
    asc, desc
  }

}
