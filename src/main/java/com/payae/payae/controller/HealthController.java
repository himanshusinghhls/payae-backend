package com.payae.payae.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String health() {
        return "PayAE Backend Running 🚀";
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public String healthJson() {
        return "{\"status\":\"UP\"}";
    }
}