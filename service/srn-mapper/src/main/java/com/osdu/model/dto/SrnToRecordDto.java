package com.osdu.model.dto;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SrnToRecordDto {

  String recordId;
  String srn;
  @ServerTimestamp
  Date created;

}
