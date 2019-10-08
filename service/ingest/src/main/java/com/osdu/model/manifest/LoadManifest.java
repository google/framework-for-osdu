package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoadManifest extends BaseJsonObject {

  @JsonProperty(value = "WorkProduct")
  WorkProduct workProduct;

  @JsonProperty(value = "WorkProductComponents")
  List<WorkProductComponent> workProductComponents;

  @JsonProperty(value = "Files")
  List<ManifestFile> files;

}
