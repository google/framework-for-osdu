package com.osdu;

import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableFeignClients(basePackages = {"com.osdu"})
public class SchemaMapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchemaMapperApplication.class, args);
    }

}
