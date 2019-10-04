package com.osdu.model.delfi.submit;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FileInput {

  @JsonProperty("filePath")
  FILE_PATH,

  @JsonProperty("byteArray")
  BYTE_ARRAY,

  @JsonProperty("folder")
  FOLDER

}
