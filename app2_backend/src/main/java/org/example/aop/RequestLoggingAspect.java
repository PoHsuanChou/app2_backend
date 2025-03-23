package org.example.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class RequestLoggingAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();

        // 獲取請求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // 如果不是 HTTP 請求上下文，直接執行原方法
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        try {
            // 記錄請求開始
            log.info("=== Request Start ===");
            log.info("Time: {}", startTime);
            log.info("URI: {} {}", request.getMethod(), request.getRequestURI());
            log.info("Headers: {}", getHeaders(request));

            // 記錄請求參數
            String requestBody = "{}";
            try {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0 && args[0] != null) {
                    requestBody = objectMapper.writeValueAsString(args[0]);
                }
            } catch (Exception e) {
                log.warn("Failed to serialize request body: {}", e.getMessage());
            }
            log.info("Request Body: {}", requestBody);

            // 執行實際的方法
            Object result = joinPoint.proceed();

            // 記錄響應
            try {
                log.info("Response Body: {}",
                        result != null ? objectMapper.writeValueAsString(result) : "null");
            } catch (Exception e) {
                log.warn("Failed to serialize response body: {}", e.getMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("Exception occurred while processing request", e);
            throw e;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            long duration = ChronoUnit.MILLIS.between(startTime, endTime);

            log.info("Duration: {} ms", duration);
            log.info("Time: {}", endTime);
            log.info("=== Request End ===");
        }
    }

    private String getHeaders(HttpServletRequest request) {
        try {
            return Collections.list(request.getHeaderNames())
                    .stream()
                    .map(headerName -> headerName + ": " + request.getHeader(headerName))
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            log.warn("Failed to get headers: {}", e.getMessage());
            return "Unable to get headers";
        }
    }
}
