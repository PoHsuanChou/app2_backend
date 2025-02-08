package org.example.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 生成唯一請求ID
        String requestId = UUID.randomUUID().toString();

        // 包裝 request 和 response 以便多次讀取
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // 記錄請求開始時間
        long startTime = System.currentTimeMillis();

        try {
            // 執行實際的請求處理
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 計算請求處理時間
            long duration = System.currentTimeMillis() - startTime;

            // 記錄請求詳情
            logRequest(requestWrapper, requestId);

            // 記錄響應詳情
            logResponse(responseWrapper, requestId, duration);

            // 複製響應內容到原始 response
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);

        RequestLog requestLog = new RequestLog();
        requestLog.setRequestId(requestId);
        requestLog.setMethod(request.getMethod());
        requestLog.setPath(request.getRequestURI());
        requestLog.setQueryString(request.getQueryString());
        requestLog.setHeaders(getHeaders(request));
        requestLog.setBody(requestBody);

        try {
            log.info("Request: {}", objectMapper.writeValueAsString(requestLog));
        } catch (Exception e) {
            logger.error("Error logging request", e);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, String requestId, long duration) {
        String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);

        ResponseLog responseLog = new ResponseLog();
        responseLog.setRequestId(requestId);
        responseLog.setStatus(response.getStatus());
        responseLog.setHeaders(getHeaders(response));
        responseLog.setBody(responseBody);
        responseLog.setDuration(duration);

        try {
            log.info("Response: {}", objectMapper.writeValueAsString(responseLog));
        } catch (Exception e) {
            logger.error("Error logging response", e);
        }
    }

    private String getHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                headers.append(headerName).append(": ").append(request.getHeader(headerName)).append(", ")
        );
        return headers.toString();
    }

    private String getHeaders(HttpServletResponse response) {
        StringBuilder headers = new StringBuilder();
        response.getHeaderNames().forEach(headerName ->
                headers.append(headerName).append(": ").append(response.getHeader(headerName)).append(", ")
        );
        return headers.toString();
    }
    @Data
    static class RequestLog {
        private String requestId;
        private String method;
        private String path;
        private String queryString;
        private String headers;
        private String body;
    }

    /**
     * 响应 DTO，包含状态码、完整响应 Body 和 API 处理耗时
     */
    @Data
    static class ResponseLog {
        private String requestId;
        private int status;
        private String headers;
        private String body;
        private long duration;
    }
}



