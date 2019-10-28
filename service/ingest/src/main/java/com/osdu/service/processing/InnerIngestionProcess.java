package com.osdu.service.processing;

import com.osdu.model.manifest.LoadManifest;
import org.springframework.messaging.MessageHeaders;

public interface InnerIngestionProcess {

  void process(String jobId, LoadManifest loadManifest, MessageHeaders headers);

}
