package com.osdu.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

@Data
public class BaseJsonObject {

  Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonAnySetter()
  void setAdditionalProperties(String key, Object value) {
    additionalProperties.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

}
