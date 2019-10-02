package com.osdu;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@SpringBootApplication
@EnableFeignClients
@Configuration
public class IngestServiceApplication {

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

}
