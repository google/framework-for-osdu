package com.osdu.function;

import com.osdu.task.CreateTaskWithName;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskFunction implements Function<Message<String>, Message<String>> {

  @Override
  public Message<String> apply(Message<String> stringMessage) {
    log.info("Start task init");

    CreateTaskWithName.createTaskWithName();

    log.info("End task init");
    return new GenericMessage<>("Task created ");
  }
}
