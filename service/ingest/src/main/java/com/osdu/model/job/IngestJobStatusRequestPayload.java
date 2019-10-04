package com.osdu.model.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import com.osdu.model.manifest.File;
import com.osdu.model.manifest.WorkProduct;
import com.osdu.model.manifest.WorkProductComponent;
import java.util.List;
import lombok.Data;

@Data
public class IngestJobStatusRequestPayload extends BaseJsonObject {

  @JsonProperty(value = "JobId")
  String jobId;

}
