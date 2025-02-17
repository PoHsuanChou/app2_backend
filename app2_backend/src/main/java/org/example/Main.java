package org.example;

import lombok.RequiredArgsConstructor;
import org.example.controller.HealthCheckController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@RequiredArgsConstructor
public class Main {
    private final HealthCheckController healthCheckController;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        System.out.println("應用程序已啟動，執行健康檢查...");
        healthCheckController.healthCheck();
    }
}