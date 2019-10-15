package com.osdu.mapper;

import com.osdu.model.SrnToRecord;
import com.osdu.model.dto.SrnToRecordDto;
import org.mapstruct.Mapper;

@Mapper
public interface SrnToRecordMapper {

  SrnToRecordDto srnToRecordToSrnToRecordDto(SrnToRecord record);

  SrnToRecord srnToRecordDtoToSrnToRecord(SrnToRecordDto recordDto);

}
