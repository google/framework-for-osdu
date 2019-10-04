package com.osdu.model.delfi.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MasterJobStatus {

  @JsonProperty("failed")
  FAILED,

  @JsonProperty("running")
  RUNNING,

  @JsonProperty("completed")
  COMPLETED
}
