package com.osdu.mapper;

import com.osdu.model.SchemaData;
import com.osdu.model.dto.SchemaDataDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
@DecoratedWith(SchemaDataMapperDecorator.class)
public interface SchemaDataMapper {

  @Mapping(target = "schema", ignore = true)
  SchemaDataDto schemaDataToSchemaDataDto(SchemaData schemaData);

  @Mapping(target = "schema", ignore = true)
  SchemaData schemaDataDtoToSchemaData(SchemaDataDto schemaDataDto);
}
