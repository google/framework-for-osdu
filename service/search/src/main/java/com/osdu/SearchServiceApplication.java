package com.osdu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients
public class SearchServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(SearchServiceApplication.class, args);
  }

}
