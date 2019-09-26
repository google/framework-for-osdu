package com.osdu.service;

import com.osdu.model.SchemaData;

public interface SrnMappingService {
  SchemaData getSchemaData(String srn);
  void save(SchemaData schemaData);
}
