package com.osdu.function;

import com.osdu.task.CreateTaskWithName;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Worker implements Function<Message<Object>, Message<String>> {

  @Override
  public Message<String> apply(Message<Object> objectMessage) {
    if(objectMessage.getHeaders().containsKey("run")){
      log.info("Start task init");

      CreateTaskWithName.createTaskWithName();

      log.info("End task init");
      return new GenericMessage<>("Task created ");
    }
    log.info("in task--------------------------");

    System.out.println("in task--------------------------");
    return new GenericMessage<>("test");
  }
}
