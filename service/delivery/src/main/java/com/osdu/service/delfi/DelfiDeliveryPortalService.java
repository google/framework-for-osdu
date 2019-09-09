package com.osdu.service.delfi;

import com.osdu.client.delfi.DelfiDeliveryClient;
import com.osdu.client.delfi.DelfiFileClient;
import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.service.PortalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class DelfiDeliveryPortalService implements PortalService {

    private final static Logger log = LoggerFactory.getLogger(PortalService.class);

    @Inject
    private DelfiDeliveryClient deliveryClient;

    @Inject
    private DelfiFileClient delfiFileClient;

    @Value("${osdu.delfi.portal.appkey}")
    private String appKey;

    @Override
    public Record getRecord(String id, String authorizationToken, String partition) {
        log.debug("Getting record with params : {}, {}, {}", id, authorizationToken, partition);
        //TODO: Remove coupling. Right now Job knows which service it needs. Replace with children classes
        //IDEA: PortalService with 2 params for method : specific data and auth info.
        //      it will have a child with DelfiAuthInfo as param which will then be passed. Job should also have a delfi child
        return deliveryClient.getRecord(id, authorizationToken, partition, appKey);
    }

    @Override
    public FileRecord getFile(String location, String authorizationToken, String partition) {
        log.debug("Getting file with params : {}, {}, {}", location, authorizationToken, partition);
        return delfiFileClient.getSignedUrlForLocation(location, authorizationToken, partition, partition, appKey);
    }

}

