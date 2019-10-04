package com.osdu.model.job;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IngestJobStatus {

  @JsonProperty("Running")
  RUNNING,
  @JsonProperty("Failed")
  FAIlED,
  @JsonProperty("Complete")
  COMPLETE

}
