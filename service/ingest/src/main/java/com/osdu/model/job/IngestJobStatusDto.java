package com.osdu.model.job;

import java.util.List;
import lombok.Data;

@Data
public class IngestJobStatusDto {

  String id;
  IngestJobStatus status;
  List<String> srns;

}
