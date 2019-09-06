package com.osdu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.osdu"})
public class DataTransferApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataTransferApplication.class, args);
    }

}
