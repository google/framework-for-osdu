package com.osdu.service;

import com.osdu.model.osdu.delivery.input.Srns;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;

public interface DeliveryService {

    DeliveryResponse getResources(Srns srns);

}
