package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class LoadManifest {

  @JsonProperty(value = "WorkProduct")
  Map<String, Object> workProduct;

  @JsonProperty(value = "Files")
  List<Map<String, Object>> files;

  @JsonProperty(value = "WorkProductComponents")
  List<Map<String, Object>> workProductComponents;

  Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonAnySetter
  void setAdditionalProperties(String key, Object value) {
    additionalProperties.put(key, value);
  }
}
