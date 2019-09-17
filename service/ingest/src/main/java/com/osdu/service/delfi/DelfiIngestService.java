package com.osdu.service.delfi;

import com.osdu.model.IngestResult;
import com.osdu.model.LoadManifest;
import com.osdu.service.IngestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiIngestService implements IngestService {

  @Override
  public IngestResult ingestManifest(LoadManifest loadManifest,
      MessageHeaders headers) {
    log.info("Request to ingest file with following parameters: {}", loadManifest);
    return null;
  }
}
