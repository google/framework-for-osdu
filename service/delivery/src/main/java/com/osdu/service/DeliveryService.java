package com.osdu.service;

import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.input.InputPayload;

import org.springframework.messaging.MessageHeaders;

public interface DeliveryService {

  DeliveryResponse getResources(InputPayload inputPayload, MessageHeaders httpHeaders);

}
