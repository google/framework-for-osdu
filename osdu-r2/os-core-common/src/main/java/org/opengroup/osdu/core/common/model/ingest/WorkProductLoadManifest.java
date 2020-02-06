package org.opengroup.osdu.core.common.model.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class WorkProductLoadManifest {

  @JsonProperty(value = "WorkProduct")
  Map<String, Object> workProduct;

  @JsonProperty(value = "Files")
  List<Map<String, Object>> files;

  @JsonProperty(value = "WorkProductComponents")
  List<Map<String, Object>> workProductComponents;

}
