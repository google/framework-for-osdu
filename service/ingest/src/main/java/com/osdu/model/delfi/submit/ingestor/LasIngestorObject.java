package com.osdu.model.delfi.submit.ingestor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LasIngestorObject {

  @JsonProperty("LASIngestor")
  LasIngestor lasIngestor;

}
