package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileDataObject extends BaseJsonObject {

  @JsonProperty("GroupTypeProperties")
  GroupTypeProperties groupTypeProperties;

}
