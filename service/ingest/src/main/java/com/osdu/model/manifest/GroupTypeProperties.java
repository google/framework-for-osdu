package com.osdu.model.manifest;

import com.osdu.model.BaseJsonObject;
import lombok.Data;

@Data
public class GroupTypeProperties extends BaseJsonObject {

  String fileSource;
  String originalFilePath;
  String stagingFilePath;
}
