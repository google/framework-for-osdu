package com.osdu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages = {"com.osdu"})
public class SchemaMapperApplication {

  public static void main(String[] args) {
    SpringApplication.run(SchemaMapperApplication.class, args);
  }

}
