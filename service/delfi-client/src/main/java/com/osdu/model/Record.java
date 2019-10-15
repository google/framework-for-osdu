package com.osdu.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public abstract class Record implements BaseRecord {

  String id;

  Map<String, Object> data;

  Map<String, Object> details = new LinkedHashMap<>();

  @JsonAnySetter
  void setDetail(String key, Object value) {
    details.put(key, value);
  }
}
