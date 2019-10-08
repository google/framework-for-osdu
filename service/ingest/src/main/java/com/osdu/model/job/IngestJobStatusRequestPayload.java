package com.osdu.model.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IngestJobStatusRequestPayload extends BaseJsonObject {

  @JsonProperty(value = "JobId")
  String jobId;

}
