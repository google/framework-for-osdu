package com.osdu;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@SpringBootApplication
@EnableFeignClients(basePackages = {"com.osdu"})
@Configuration
public class IngestServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(IngestServiceApplication.class, args);
  }

  @Bean
  public Storage googleCloudStorage() {
    // Assuming that Application Default Credentials have been set
    // according to https://cloud.google.com/docs/authentication/production
    return StorageOptions.getDefaultInstance().getService();
  }

}
