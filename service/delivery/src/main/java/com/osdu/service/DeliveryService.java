package com.osdu.service;

import com.osdu.exception.OsduUrlException;
import com.osdu.model.osdu.manifest.DeliveryResult;
import com.osdu.model.osdu.manifest.ManifestObject;

public interface DeliveryService {

    DeliveryResult getResources(ManifestObject manifestObject) throws OsduUrlException;

}
