package com.osdu.model.osdu.delivery.delfi;

import com.osdu.model.BaseRecord;
import lombok.Data;

@Data
public class ProcessingResult {

  ProcessingResultStatus processingResultStatus;
  String fileLocation;
  BaseRecord data;
  String srn;

}
