package com.osdu.model.delfi.status;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class JobInfo {

  String jobId;
  String fileName;
  LocalDateTime timestamp;
  String currentJobStatus;
  MasterJobStatus masterJobStatus;

}
