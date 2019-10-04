package com.osdu.model.delfi.submit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitJobResult {

  String jobId;
  String srn;

}
