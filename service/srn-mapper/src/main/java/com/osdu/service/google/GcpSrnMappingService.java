package com.osdu.service.google;

import com.osdu.mapper.SchemaDataMapper;
import com.osdu.model.SchemaData;
import com.osdu.model.dto.SchemaDataDto;
import com.osdu.repository.SchemaDataRepository;
import com.osdu.service.SrnMappingService;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GcpSrnMappingService implements SrnMappingService {

  @Inject
  SchemaDataRepository schemaDataRepository;

  @Inject
  @Named
  SchemaDataMapper schemaDataMapper;

  @Override
  public SchemaData getSchemaDataForSrn(String srn) {
    log.debug("Request to get SchemaData by SRN : {}", srn);
    final SchemaDataDto bySrn = schemaDataRepository.findBySrn(srn);
    log.debug("Found SchemaData : {}", bySrn);
    return schemaDataMapper.schemaDataDtoToSchemaData(bySrn);
  }

  @Override
  public void saveSchemaData(SchemaData schemaData) {
    log.debug("Request to save SchemaData : {}", schemaData);
    schemaDataRepository
        .save(schemaDataMapper.schemaDataToSchemaDataDto(schemaData));
  }


}
