package com.osdu.model.delfi.enrich;

import com.osdu.model.Record;
import com.osdu.model.delfi.IngestedFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrichedFile {

  IngestedFile ingestedFile;
  Record record;

}
