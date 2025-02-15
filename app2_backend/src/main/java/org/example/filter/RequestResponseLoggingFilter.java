package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // 最高優先級，最先執行
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Instant start = Instant.now();

        // Wrap the request and response to be able to read the body multiple times
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(response);

        // Log Request Details
        String requestBody = wrappedRequest.getRequestBody();
        log.info("Received {} request for URL: {}", request.getMethod(), request.getRequestURI());
        log.info("Request Headers: {}", request.getHeaderNames());
        log.info("Request Body: {}", requestBody);

        // Proceed with the filter chain
        filterChain.doFilter(wrappedRequest, wrappedResponse);

        // Compute execution time
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();

        // Log Response Details
        String responseBody = new String(wrappedResponse.getBody(), StandardCharsets.UTF_8);
        log.info("Response for {} request to {} with status: {}. Time taken: {} ms",
                request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        log.info("Response Headers: {}", response.getHeaderNames());
        log.info("Response Body: {}", responseBody);

        // Write the response body back to the original response
        ServletOutputStream out = response.getOutputStream();
        out.write(wrappedResponse.getBody());
        out.flush();
    }
}