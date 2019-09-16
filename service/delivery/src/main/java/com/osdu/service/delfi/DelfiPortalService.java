package com.osdu.service.delfi;

import com.osdu.client.delfi.DelfiDeliveryClient;
import com.osdu.client.delfi.DelfiFileClient;
import com.osdu.exception.OSDUException;
import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.service.PortalService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiPortalService implements PortalService {

  @Inject
  DelfiDeliveryClient deliveryClient;

  @Inject
  DelfiFileClient delfiFileClient;

  @Value("${osdu.delfi.portal.appkey}")
  String appKey;

  @Override
  public Record getRecord(String id, String authorizationToken, String partition) {
    log.debug("Getting record with params : {}, {}, {}", id, authorizationToken, partition);
    if (id == null || authorizationToken == null || partition == null) {
      throw new OSDUException(String.format("Invalid parameters passed to client :"
          + " id:  %s, authorizationToken : %s, partition: %s", id, authorizationToken, partition));
    }
    return deliveryClient.getRecord(id, authorizationToken, partition, appKey);
  }

  @Override
  public FileRecord getFile(String location, String authorizationToken, String partition) {
    log.debug("Getting file with params : {}, {}, {}", location, authorizationToken, partition);
    if (location == null || authorizationToken == null || partition == null) {
      throw new OSDUException(String.format("Invalid parameters passed to client :"
              + " location:  %s, authorizationToken : %s, partition: %s", location, authorizationToken,
          partition));
    }
    return delfiFileClient
        .getSignedUrlForLocation(location, authorizationToken, partition, partition, appKey);
  }

}

