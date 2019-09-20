package com.osdu.service;

import com.osdu.model.SchemaData;

public interface SrnMappingService {
  String mapSRNToKind(String srn);
  SchemaData getSchemaDataForSrn(String srn);
}
