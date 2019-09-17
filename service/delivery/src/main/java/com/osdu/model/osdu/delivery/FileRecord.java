package com.osdu.model.osdu.delivery;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public abstract class FileRecord {

  Map<String, Object> details = new LinkedHashMap<>();

  @JsonAnySetter
  void setDetail(String key, Object value) {
    details.put(key, value);
  }
}
