package com.osdu.function;

import com.osdu.exception.OsduUrlException;
import com.osdu.model.osdu.manifest.DeliveryResult;
import com.osdu.model.osdu.manifest.ManifestObject;
import com.osdu.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DataTransferFunction implements Function<Message<ManifestObject>, Message<DeliveryResult>> {

    private final static Logger log = LoggerFactory.getLogger(DataTransferFunction.class);

    @Autowired
    private DeliveryService deliveryService;

    @Override
    public Message<DeliveryResult> apply(Message<ManifestObject> messageSource) {
        try {
            log.debug("Received request: {}", messageSource);

            DeliveryResult resource = deliveryService.getResources(messageSource.getPayload());

            return new GenericMessage<>(resource);

        } catch (OsduUrlException e) {
            throw new RuntimeException(String.format("Exception while processing request: %s", messageSource), e);
        }
    }
}
