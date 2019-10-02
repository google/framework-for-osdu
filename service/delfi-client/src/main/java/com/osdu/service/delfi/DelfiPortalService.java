package com.osdu.service.delfi;

import com.osdu.client.DelfiDeliveryClient;
import com.osdu.client.DelfiFileClient;
import com.osdu.exception.OsduException;
import com.osdu.model.FileRecord;
import com.osdu.model.Record;
import com.osdu.model.delfi.DelfiFileRecord;
import com.osdu.model.delfi.DelfiRecord;
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
  DelfiFileClient fileClient;

  @Value("${osdu.delfi.portal.appkey}")
  String appKey;

  @Override
  public Record getRecord(String id, String authorizationToken, String partition) {
    log.debug("Getting record with params : {}, {}, {}", id, authorizationToken, partition);
    if (id == null || authorizationToken == null || partition == null) {
      throw new OsduException(String.format("Invalid parameters passed to client :"
          + " id:  %s, authorizationToken : %s, partition: %s", id, authorizationToken, partition));
    }
    DelfiRecord record = deliveryClient.getRecord(id, authorizationToken, partition, appKey);
    log.debug("Got record: /n" + record.toString());

    return record;
  }

  @Override
  public FileRecord getFile(String location, String authorizationToken, String partition) {
    log.debug("Getting file with params : {}, {}, {}", location, authorizationToken, partition);
    if (location == null || authorizationToken == null || partition == null) {
      throw new OsduException(String.format("Invalid parameters passed to client :"
              + " location:  %s, authorizationToken : %s, partition: %s", location,
          authorizationToken, partition));
    }
    DelfiFileRecord fileRecord = fileClient
        .getSignedUrlForLocation(location, authorizationToken, partition, partition, appKey);
    log.debug("Got file record: /n" + fileRecord.toString());
    return fileRecord;
  }

  @Override
  public Record putRecord(String id, Record record, String authorizationToken, String partition) {
    log.debug("Getting record with params : {}, {}, {}", id, authorizationToken, partition);
    if (id == null || authorizationToken == null || partition == null) {
      throw new OsduException(String.format("Invalid parameters passed to client :"
          + " id:  %s, authorizationToken : %s, partition: %s", id, authorizationToken, partition));
    }
    DelfiRecord resultRecord = deliveryClient.putRecord(record, authorizationToken, partition,
        appKey);
    log.debug("Put record finished: " + resultRecord.toString());
    return resultRecord;
  }
}

