package com.osdu.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

@Data
public abstract class FileRecord implements BaseRecord {

  Map<String, Object> details = new LinkedHashMap<>();

  @JsonAnySetter
  void setDetail(String key, Object value) {
    details.put(key, value);
  }
}
