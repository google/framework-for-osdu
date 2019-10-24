package com.osdu.model.delfi;

import java.util.List;
import lombok.Data;

@Data
public class SaveRecordsResult {

  int recordCount;
  List<String> recordIds;
  List<String> skippedRecordIds;

}
