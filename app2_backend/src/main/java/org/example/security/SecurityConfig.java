package org.example.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor  // Lombok 注解，自動生成帶有 final 字段的構造函數
public class SecurityConfig {

    // JWT 過濾器，用於處理 JWT 令牌的驗證
    private final JwtAuthenticationFilter jwtAuthFilter;
    // 認證提供者，處理用戶認證邏輯
//    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 關閉 CSRF 保護，因為我們使用 JWT 做無狀態認證
                .csrf(csrf -> csrf.disable())

                // 配置請求授權規則
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/register",// 認證相關的 API
                                "/v3/api-docs/**", // Swagger 文檔
                                "/swagger-ui/**"   // Swagger UI
                        ).permitAll()          // 以上路徑允許未認證訪問
                        .anyRequest().authenticated()  // 其他所有請求需要認證
                )

                // 配置會話管理
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 使用無狀態會話
                )

                // 設置認證提供者 //如果是標準的用戶名/密碼認證 → 使用 DaoAuthenticationProvider
//                .authenticationProvider(authenticationProvider)

                // 添加 JWT 過濾器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 異常處理配置
                .exceptionHandling(exception -> exception
                        // 處理未認證異常
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 錯誤
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                        // 處理權限不足異常
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 錯誤
                            response.getWriter().write("{\"error\": \"Access Denied\"}");
                        })
                );

        return http.build();
    }

}
