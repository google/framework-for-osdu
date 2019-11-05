package com.osdu.config;

import com.osdu.async.CustomAsyncExceptionHandler;
import com.osdu.model.osdu.delivery.property.OsduDeliveryProperties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

  public static final String DATA_PROCESSING_EXECUTOR = "dataProcessingExecutor";

  @Bean(name = DATA_PROCESSING_EXECUTOR)
  public Executor dataProcessingExecutor(OsduDeliveryProperties deliveryProperties) {
    return Executors.newFixedThreadPool(deliveryProperties.getThreadPoolCapacity());
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new CustomAsyncExceptionHandler();
  }

}
