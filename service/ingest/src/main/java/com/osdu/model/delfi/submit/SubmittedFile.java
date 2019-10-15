package com.osdu.model.delfi.submit;

import com.osdu.model.delfi.signed.SignedFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmittedFile {

  SignedFile signedFile;
  String srn;
  String jobId;

}
