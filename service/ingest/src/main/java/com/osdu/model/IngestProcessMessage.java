package com.osdu.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.osdu.jackson.Base64Deserializer;
import com.osdu.model.job.IngestMessage;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestProcessMessage {

  Map<String, String> attributes;

  @JsonDeserialize(using = Base64Deserializer.class)
  @JsonProperty("data")
  IngestMessage ingestMessage;

  String messageId;

}
