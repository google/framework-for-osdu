package com.osdu.async;

import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

  @Override
  public void handleUncaughtException(Throwable ex, Method method, Object... objects) {
    String msg = "Async Exception**************************************************************"
        + "\nmethod happen: " + method
        + "\nmethod params: " + Arrays.toString(objects)
        + "\nException class: {}" + ex.getClass().getName()
        + "\nex.getMessage(): {}" + ex.getMessage()
        + "\n**************************************************************";
    log.error(msg, ex);
  }
}
