package com.osdu.model.type.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OsduObjectData {

  @JsonIgnore
  GroupTypeProperties groupTypeProperties;

  @JsonIgnore
  IndividualTypeProperties individualTypeProperties;

  @JsonIgnore
  ExtensionProperties extensionProperties;

  @JsonProperty("GroupTypeProperties")
  public GroupTypeProperties getGroupTypeProperties() {
    return groupTypeProperties;
  }

  @JsonProperty("IndividualTypeProperties")
  public IndividualTypeProperties getIndividualTypeProperties() {
    return individualTypeProperties;
  }

  @JsonProperty("ExtensionProperties")
  public ExtensionProperties getExtensionProperties() {
    return extensionProperties;
  }

}
