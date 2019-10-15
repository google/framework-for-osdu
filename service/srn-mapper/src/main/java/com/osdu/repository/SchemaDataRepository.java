package com.osdu.repository;

import com.osdu.model.dto.SchemaDataDto;

public interface SchemaDataRepository {

  SchemaDataDto findExactByTypeId(String typeId);

  SchemaDataDto findLastByTypeId(String typeId);

  void save(SchemaDataDto schemaData);

}
