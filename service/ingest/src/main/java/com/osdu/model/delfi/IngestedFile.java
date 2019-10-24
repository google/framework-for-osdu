package com.osdu.model.delfi;

import com.osdu.model.delfi.submit.SubmittedFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngestedFile {

  SubmittedFile submittedFile;
  String recordId;

}
