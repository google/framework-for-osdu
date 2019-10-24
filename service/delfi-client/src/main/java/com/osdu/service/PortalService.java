package com.osdu.service;

import com.osdu.model.Record;
import com.osdu.model.delfi.DelfiFile;

public interface PortalService {

  Record getRecord(String id, String autorizationToken, String partition);

  Record putRecord(Record record, String authorizationToken, String partition);

  DelfiFile getFile(String location, String authorizationToken, String partition);

}
