package com.osdu.model.job;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;
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
  String summary;
  List<String> srns;
  @ServerTimestamp
  Date created;

}
