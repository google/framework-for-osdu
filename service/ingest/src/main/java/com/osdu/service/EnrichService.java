package com.osdu.service;

import com.osdu.model.IngestHeaders;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;

public interface EnrichService {

  EnrichedFile enrichRecord(IngestedFile submittedFile, RequestMeta requestMeta,
      IngestHeaders headers);

}
