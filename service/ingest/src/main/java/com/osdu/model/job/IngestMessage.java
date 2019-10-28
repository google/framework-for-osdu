package com.osdu.model.job;

import com.osdu.model.manifest.LoadManifest;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestMessage {

  String ingestJobId;
  LoadManifest loadManifest;
  Map<String, Object> headers;

}
