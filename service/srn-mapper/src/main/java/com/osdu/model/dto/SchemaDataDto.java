package com.osdu.model.dto;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;
import lombok.Data;

@Data
public class SchemaDataDto {

  String srn;
  String kind;
  String schema;
  @ServerTimestamp
  Date created;

}
