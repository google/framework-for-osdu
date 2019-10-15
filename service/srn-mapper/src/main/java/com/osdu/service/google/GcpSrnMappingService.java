package com.osdu.service.google;

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
    log.debug("Found SchemaData: {}", schemaDataDto);

    return schemaDataDto == null ? null : schemaDataMapper.schemaDataDtoToSchemaData(schemaDataDto);
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
