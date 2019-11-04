package com.osdu.function;

import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.input.InputPayload;
import com.osdu.service.DeliveryService;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataTransferFunction implements Function<Message<InputPayload>,
    Message<DeliveryResponse>> {

  @Inject
  private DeliveryService deliveryService;

  @Override
  public Message<DeliveryResponse> apply(Message<InputPayload> messageSource) {
    log.info("Received request: {}", messageSource);
    DeliveryResponse resource = deliveryService.getResources(messageSource.getPayload(),
        messageSource.getHeaders());

    log.info("Delivery response: {}", resource);
    return new GenericMessage<>(resource);
  }
}
