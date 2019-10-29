package com.osdu.service.processing;

import com.osdu.model.IngestHeaders;
import com.osdu.model.manifest.LoadManifest;

public interface InnerIngestionProcess {

  void process(String jobId, LoadManifest loadManifest, IngestHeaders headers);

}
