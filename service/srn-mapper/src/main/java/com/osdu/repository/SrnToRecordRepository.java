package com.osdu.repository;

import com.osdu.model.dto.SrnToRecordDto;

public interface SrnToRecordRepository {

  SrnToRecordDto findBySrn(String srn);

  void save(SrnToRecordDto record);

}
