package com.osdu.service.delfi;

import com.osdu.client.DelfiFileClient;
import com.osdu.client.DelfiStorageClient;
import com.osdu.exception.OsduException;
import com.osdu.model.Record;
import com.osdu.model.delfi.DelfiFile;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.delfi.SaveRecordsResult;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.service.PortalService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiPortalService implements PortalService {

  final DelfiStorageClient storageClient;

  final DelfiFileClient fileClient;

  final DelfiPortalProperties portalProperties;

  @Override
  public Record getRecord(String id, String authorizationToken, String partition) {
    log.debug("Getting record with params : {}, {}, {}", id, authorizationToken, partition);
    if (id == null || authorizationToken == null || partition == null) {
      throw new OsduException(String.format("Invalid parameters passed to client :"
          + " id:  %s, authorizationToken : %s, partition: %s", id, authorizationToken, partition));
    }
    DelfiRecord record = storageClient.getRecord(id, authorizationToken, partition,
        portalProperties.getAppKey());
    log.debug("Got record: /n" + record.toString());

    return record;
  }

  @Override
  public DelfiFile getFile(String location, String authorizationToken, String partition) {
    log.debug("Getting file with params : {}, {}, {}", location, authorizationToken, partition);
    if (location == null || authorizationToken == null || partition == null) {
      throw new OsduException(String.format("Invalid parameters passed to client :"
              + " location:  %s, authorizationToken : %s, partition: %s", location,
          authorizationToken, partition));
    }
    DelfiFile delfiFile = fileClient.getSignedUrlForLocation(location, authorizationToken,
        portalProperties.getAppKey(), partition);
    log.debug("Got Delfi file: /n" + delfiFile);
    return delfiFile;
  }

  @Override
  public Record putRecord(Record record, String authorizationToken, String partition) {
    log.debug("Put record with params : {}, {}, {}", record, authorizationToken, partition);
    if (authorizationToken == null || partition == null) {
      throw new OsduException(String.format("Invalid parameters passed to client :"
          + " record:  %s, authorizationToken : %s, partition: %s", record, authorizationToken,
          partition));
    }
    SaveRecordsResult saveResult = storageClient.putRecords(Collections.singletonList(record),
        authorizationToken, partition, portalProperties.getAppKey());
    log.debug("Save records result: {}", saveResult);
    Record resultRecord = getRecord(saveResult.getRecordIds().get(0), authorizationToken, partition);
    log.debug("Put record finished: " + resultRecord);
    return resultRecord;
  }
}

