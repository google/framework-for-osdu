package com.osdu.service.delfi;

import com.osdu.model.Record;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.service.EnrichService;
import com.osdu.service.PortalService;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class DelfiEnrichService implements EnrichService {

  @Inject
  PortalService portalService;

  @Override
  public Record enrichRecord(String odesId, LoadManifest loadManifest, String authorizationToken,
      String partition) {

    final Record record = portalService.getRecord(odesId, authorizationToken, partition);

    populateRecordWithManifest(loadManifest, record);

    return portalService.putRecord(odesId, record, authorizationToken, partition);
  }

  private Record populateRecordWithManifest(LoadManifest loadManifest, Record record) {
    record.getDetails().put("WorkProduct", loadManifest.getWorkProduct());
    record.getDetails().put("WorkProductComponents", loadManifest.getWorkProductComponents());
    record.getDetails().put("Files", loadManifest.getFiles());
    record.getDetails().putAll(loadManifest.getAdditionalProperties());

    return record;
  }
}
