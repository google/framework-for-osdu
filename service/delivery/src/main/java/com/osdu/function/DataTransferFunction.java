package com.osdu.function;

import com.osdu.exception.OsduUrlException;
import com.osdu.model.osdu.delivery.input.Srns;
import com.osdu.model.osdu.delivery.response.DeliveryResponse;
import com.osdu.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DataTransferFunction implements Function<Message<Srns>, Message<DeliveryResponse>> {

    private final static Logger log = LoggerFactory.getLogger(DataTransferFunction.class);

    @Autowired
    private DeliveryService deliveryService;

    @Override
    public Message<DeliveryResponse> apply(Message<Srns> messageSource) {

        log.debug("Received request: {}", messageSource);
        DeliveryResponse resource = deliveryService.getResources(messageSource.getPayload());

        return new GenericMessage<>(resource);
    }
}
