package com.osdu.model.osdu.delivery.delfi;

import lombok.Data;

@Data
public class ProcessingResult {

  ProcessingResultStatus processingResultStatus;
  String fileLocation;
  Object data;
  String srn;

}
