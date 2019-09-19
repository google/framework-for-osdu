package com.osdu.service;

import com.osdu.model.IngestResult;
import com.osdu.model.manifest.LoadManifest;
import org.springframework.messaging.MessageHeaders;

public interface IngestService {

  IngestResult ingestManifest(LoadManifest loadManifest,
      MessageHeaders headers);

}
