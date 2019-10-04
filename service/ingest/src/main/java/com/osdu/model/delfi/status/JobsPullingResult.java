package com.osdu.model.delfi.status;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsPullingResult {

  List<String> runningJobs;
  List<JobStatus> failedJobs;
  List<JobStatus> completedJobs;

}
