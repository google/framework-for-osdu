package com.osdu.service;

import com.osdu.exception.OsduUrlException;
import com.osdu.model.osdu.manifest.DeliveryResult;
import com.osdu.model.osdu.manifest.ManifestObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Service
public class DelfiDeliveryService implements DeliveryService {

    private final static Logger log = LoggerFactory.getLogger(DelfiDeliveryService.class);

    @Value("${osdu.download.resource.url}")
    private String downloadResourceLocation;

    @Override
    public DeliveryResult getResources(ManifestObject manifestObject) throws OsduUrlException {

        String resultString = StringUtils.EMPTY;
        try {
            log.debug("Processing manifest: {}", manifestObject);

            resultString = downloadResourceLocation;
            return DeliveryResult.builder()
                    .resourceLink(new URL(resultString))
                    .build();
        } catch (MalformedURLException e) {
            throw new OsduUrlException(String.format("Can not construct URL from string: %s", resultString));
        }
    }
}
