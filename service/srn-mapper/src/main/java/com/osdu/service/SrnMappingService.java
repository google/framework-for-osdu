package com.osdu.service;

import com.osdu.model.SchemaData;
import com.osdu.model.SrnToRecord;

public interface SrnMappingService {

  SchemaData getSchemaData(String typeId);

  void saveSchemaData(SchemaData schemaData);

  SrnToRecord getSrnToRecord(String srn);

  void saveSrnToRecord(SrnToRecord record);

}
