package com.osdu.model.job;

import com.google.cloud.Timestamp;
import com.osdu.model.BaseRecord;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestJob {

  String id;
  IngestJobStatus status;
  List<BaseRecord> records;
  Timestamp created;

}
