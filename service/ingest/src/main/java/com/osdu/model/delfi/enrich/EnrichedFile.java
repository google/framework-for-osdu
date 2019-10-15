package com.osdu.model.delfi.enrich;

import com.osdu.model.Record;
import com.osdu.model.delfi.submit.SubmittedFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrichedFile {

  SubmittedFile submittedFile;
  Record record;

}
