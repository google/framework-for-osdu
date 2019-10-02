package com.osdu.service;

import com.osdu.model.Record;
import com.osdu.model.manifest.LoadManifest;

public interface EnrichService {

  Record enrichRecord(String odesId, LoadManifest loadManifest, String authorizationToken,
      String partition);

}
