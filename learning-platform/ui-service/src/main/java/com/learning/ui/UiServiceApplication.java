package com.learning.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Serves the static web UI (login page + lectures dashboard) from src/main/resources/static.
 * Open it through the gateway: http://localhost:8080
 */
@SpringBootApplication
public class UiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UiServiceApplication.class, args);
    }
}
