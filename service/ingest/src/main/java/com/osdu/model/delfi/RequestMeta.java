package com.osdu.model.delfi;

import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestMeta {

  String authorizationToken;
  String partition;
  String legalTags;
  SchemaData schemaData;
  Map<String, String> userGroupEmailByName;
  ResourceTypeId resourceTypeId;

}
