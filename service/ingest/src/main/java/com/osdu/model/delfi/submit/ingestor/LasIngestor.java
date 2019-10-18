package com.osdu.model.delfi.submit.ingestor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_DEFAULT)
public class LasIngestor {

  String parentWellName;

  boolean createRawWellRecord;

  String parentWellRecordId;

  List<String> parentRecordIds;

  @JsonProperty("isTrajectory")
  boolean isTrajectory;

  String datasetDescriptor;

}
