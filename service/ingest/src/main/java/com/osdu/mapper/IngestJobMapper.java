package com.osdu.mapper;

import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatusDto;
import org.mapstruct.Mapper;

@Mapper
public interface IngestJobMapper {

  IngestJobStatusDto toStatusDto(IngestJob job);

}
