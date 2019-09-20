package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.Map;
import lombok.Data;

@Data
public class LoadManifest {

  Map<String, Object> workProduct;
  Map<String, Object> files;
  Map<String, Object> workProductComponents;
  Map<String, Object> additionalParameters;

  @JsonAnyGetter
  public Map<String, Object> otherFields() {
    return additionalParameters;
  }

  @JsonAnySetter
  public void setOtherField(String name, Object value) {
    additionalParameters.put(name, value);
  }
  
}
