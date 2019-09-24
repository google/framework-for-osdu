package com.osdu.service;

import com.osdu.model.SchemaData;

public interface SrnMappingService {
  SchemaData getSchemaDataForSrn(String srn);
  void saveSchemaData(SchemaData schemaData);
}
