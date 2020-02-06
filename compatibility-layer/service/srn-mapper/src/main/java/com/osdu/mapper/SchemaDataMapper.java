/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
