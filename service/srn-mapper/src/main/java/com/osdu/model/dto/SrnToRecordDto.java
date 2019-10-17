package com.osdu.model.dto;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrnToRecordDto {

  String recordId;
  String srn;
  @ServerTimestamp
  Date created;

}
