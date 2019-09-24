package com.osdu.model.manifest;

import java.util.Map;
import lombok.Data;

@Data
public class LoadManifest {

  Map<String, Object> workProduct;
  Map<String, Object> files;
  Map<String, Object> workProductComponents;
}
