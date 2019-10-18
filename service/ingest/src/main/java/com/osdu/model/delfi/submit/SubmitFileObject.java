package com.osdu.model.delfi.submit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class SubmitFileObject {

  String kind;

  String acl;

  @JsonProperty("legaltags")
  String legalTags;

  String filePath;

  FileInput fileInput;

  String ingestorRoutines;

  String additionalProperties;

}
