package com.osdu.model.delfi;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.osdu.model.delfi.submit.FileInput;
import lombok.Data;

@Data
public class SubmitFileObject {

  String ingestorRoutines;
  String kind;
  String filePath;
  @JsonAlias("legaltags")
  String legalTags;
  String acl;
  FileInput fileInput;
  String additionalProperties;

}
