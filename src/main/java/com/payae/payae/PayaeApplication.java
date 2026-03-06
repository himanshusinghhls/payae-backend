package com.payae.payae;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class PayaeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayaeApplication.class, args);
    }

}