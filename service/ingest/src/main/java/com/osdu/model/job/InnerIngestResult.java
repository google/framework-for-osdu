package com.osdu.model.job;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InnerIngestResult {

  IngestJobStatus jobStatus;
  List<String> srns;

}
