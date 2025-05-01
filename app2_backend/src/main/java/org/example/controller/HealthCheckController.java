package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repository.MongoHealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final MongoHealthIndicator mongoHealthIndicator;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        System.out.println("開始執行健康檢查...");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        
        try {
            mongoHealthIndicator.health();
            response.put("database", "UP");
            System.out.println("健康檢查完成：所有服務正常運行");
        } catch (Exception e) {
            System.err.println("健康檢查失敗：" + e.getMessage());
            response.put("database", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}

