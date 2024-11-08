package com.bolivariano.microservice.agrocalidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jms.annotation.EnableJms;


import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication()
@EnableConfigurationProperties()
@EnableJms
public class AgrocalidadApplication {


    public static void main(String... args) {
        SpringApplication.run(AgrocalidadApplication.class, args);
        log.info("📦 MicroService running");
    }

}