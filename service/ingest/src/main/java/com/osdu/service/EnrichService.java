package com.osdu.service;

import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.delfi.submit.SubmittedFile;
import org.springframework.messaging.MessageHeaders;

public interface EnrichService {

  EnrichedFile enrichRecord(SubmittedFile submittedFile, RequestMeta requestMeta,
      MessageHeaders headers);

}
