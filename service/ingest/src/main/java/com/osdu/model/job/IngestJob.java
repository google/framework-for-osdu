package com.osdu.model.job;

import com.osdu.model.BaseRecord;
import java.util.List;
import lombok.Data;

@Data
public class IngestJob {

  String id;
  IngestJobStatus status;
  List<BaseRecord> records;
}
