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

package com.osdu.service.google;

import static java.lang.String.format;

import com.osdu.exception.OsduNotFoundException;
import com.osdu.mapper.SchemaDataMapper;
import com.osdu.mapper.SrnToRecordMapper;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import com.osdu.model.SrnToRecord;
import com.osdu.model.dto.SchemaDataDto;
import com.osdu.model.dto.SrnToRecordDto;
import com.osdu.repository.SchemaDataRepository;
import com.osdu.repository.SrnToRecordRepository;
import com.osdu.service.SrnMappingService;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcpSrnMappingService implements SrnMappingService {

  final SchemaDataRepository schemaDataRepository;
  final SrnToRecordRepository srnToRecordRepository;

  @Named
  final SchemaDataMapper schemaDataMapper;
  @Named
  final SrnToRecordMapper srnToRecordMapper;

  @Override
  public SchemaData getSchemaData(String typeId) {
    log.debug("Request to get SchemaData by typeId: {}", typeId);
    ResourceTypeId resourceTypeId = new ResourceTypeId(typeId);
    SchemaDataDto schemaDataDto = resourceTypeId.hasVersion()
        ? schemaDataRepository.findExactByTypeId(typeId)
        : schemaDataRepository.findLastByTypeId(typeId);

    if (schemaDataDto == null) {
      throw new OsduNotFoundException(format("Can not find schema data for type - %s", typeId));
    }
    log.debug("Found SchemaData: {}", schemaDataDto);

    return schemaDataMapper.schemaDataDtoToSchemaData(schemaDataDto);
  }

  @Override
  public void saveSchemaData(SchemaData schemaData) {
    log.debug("Request to save SchemaData : {}", schemaData);
    schemaDataRepository
        .save(schemaDataMapper.schemaDataToSchemaDataDto(schemaData));
  }

  @Override
  public SrnToRecord getSrnToRecord(String srn) {
    log.debug("Request to get SrnToRecord by srn: {}", srn);
    SrnToRecordDto srnToRecordDto = srnToRecordRepository.findBySrn(srn);
    log.debug("Found SrnToRecord: {}", srnToRecordDto);
    return srnToRecordDto == null ? null
        : srnToRecordMapper.srnToRecordDtoToSrnToRecord(srnToRecordDto);
  }

  @Override
  public void saveSrnToRecord(SrnToRecord record) {
    log.debug("Request to save SrnToRecord: {}", record);
    srnToRecordRepository.save(srnToRecordMapper.srnToRecordToSrnToRecordDto(record));
  }

}
