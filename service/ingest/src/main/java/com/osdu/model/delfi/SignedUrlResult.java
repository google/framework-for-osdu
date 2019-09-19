package com.osdu.model.delfi;

import lombok.Data;

@Data
public class SignedUrlResult {

  Integer responseCode;
  String locationUrl;
  String relativeFilePath;

}
