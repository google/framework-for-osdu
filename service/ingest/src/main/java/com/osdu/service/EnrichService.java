package com.osdu.service;

import com.osdu.model.Record;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.manifest.LoadManifest;
import java.util.List;

public interface EnrichService {

  Record enrichRecord(String odesId, LoadManifest loadManifest, String authorizationToken,
      String partition);

  List<Record> enrichRecords(List<SubmittedFile> ingestRecords, RequestMeta requestMeta);

}
