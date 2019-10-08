package com.osdu.model.delfi;

import com.osdu.model.Record;
import com.osdu.model.delfi.submit.SubmittedFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngestRecord {

  SubmittedFile submittedFile;
  Record record;

}
