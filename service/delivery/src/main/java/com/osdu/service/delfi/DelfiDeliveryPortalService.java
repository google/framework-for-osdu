package com.osdu.service.delfi;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;

public interface DelfiDeliveryPortalService {

    Record getRecord(String id, String autorizationToken, String partition);

    FileRecord getFile(String location, String autorizationToken, String parition);

}

