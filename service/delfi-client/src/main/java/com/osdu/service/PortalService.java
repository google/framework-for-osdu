package com.osdu.service;

import com.osdu.model.FileRecord;
import com.osdu.model.Record;

public interface PortalService {

  Record getRecord(String id, String autorizationToken, String partition);

  Record putRecord(Record record, String authorizationToken, String partition);

  FileRecord getFile(String location, String authorizationToken, String partition);
}
