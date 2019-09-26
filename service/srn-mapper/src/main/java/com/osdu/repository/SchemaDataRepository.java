package com.osdu.repository;

import com.osdu.model.dto.SchemaDataDto;

public interface SchemaDataRepository {

  SchemaDataDto findBySrn(String srn);

  void save(SchemaDataDto schemaData);

}
