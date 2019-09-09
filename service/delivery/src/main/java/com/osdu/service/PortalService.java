package com.osdu.service;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;

public interface PortalService {

    Record getRecord(String id, String autorizationToken, String partition);

    FileRecord getFile(String location, String autorizationToken, String partition);

}
