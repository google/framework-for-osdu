package com.osdu.model.delfi.signed;

import java.net.URL;
import lombok.Data;

@Data
public class SignedUrlResult {

  Integer responseCode;
  URL locationUrl;
  String relativeFilePath;

}
