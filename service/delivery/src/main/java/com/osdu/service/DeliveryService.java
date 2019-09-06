package com.osdu.service;

import com.osdu.model.osdu.delivery.input.Srns;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import org.springframework.messaging.MessageHeaders;

public interface DeliveryService {

    DeliveryResponse getResources(Srns srns, MessageHeaders httpHeaders);

}
