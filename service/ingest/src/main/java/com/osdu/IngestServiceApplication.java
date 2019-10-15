package com.osdu;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.osdu.async.CustomAsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableFeignClients
@EnableAsync
@Configuration
public class IngestServiceApplication implements AsyncConfigurer {

  public static void main(String[] args) {
    SpringApplication.run(IngestServiceApplication.class, args);
  }

  /**
   * Object to work with Google Cloud Storage.
   */
  @Bean
  public Storage googleCloudStorage() {
    return StorageOptions.getDefaultInstance().getService();
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new CustomAsyncExceptionHandler();
  }

}
