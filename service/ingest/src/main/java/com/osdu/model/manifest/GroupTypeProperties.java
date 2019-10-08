package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupTypeProperties extends BaseJsonObject {

  @JsonProperty("FileSource")
  String fileSource;

  @JsonProperty("OriginalFilePath")
  String originalFilePath;

  @JsonProperty("StagingFilePath")
  String stagingFilePath;

}
