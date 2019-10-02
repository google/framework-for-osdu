package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import java.util.List;
import lombok.Data;

@Data
public class LoadManifest extends BaseJsonObject {

  @JsonProperty(value = "WorkProduct")
  WorkProduct workProduct;

  @JsonProperty(value = "Files")
  List<File> files;

  @JsonProperty(value = "WorkProductComponents")
  List<WorkProductComponent> workProductComponents;
}
