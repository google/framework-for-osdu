package com.osdu.function;

import java.util.function.Function;
import org.springframework.messaging.Message;

public class IngestFunction  implements Function<Message<Object>, Message<Object>> {

  @Override
  public Message<Object> apply(Message<Object> objectMessage) {
    return null;
  }
}
