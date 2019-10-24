package com.osdu.service;

import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import org.springframework.messaging.MessageHeaders;

public interface EnrichService {

  EnrichedFile enrichRecord(IngestedFile submittedFile, RequestMeta requestMeta,
      MessageHeaders headers);

}
