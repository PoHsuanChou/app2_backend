package org.example.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // 配置請求授權規則
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI v3 (OpenAPI) - 確保這些路徑在JWT過濾器之前
                        .requestMatchers("/static/uploads/**").permitAll()
                        .requestMatchers(
                                "/",
//                                "/api/auth/register",
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/webjars/**",
                                "/ws/**",// 允許 WebSocket 端點的訪問
                                "/ws",
                                "/ws/**",
//                                "/ws/info/**",     // SockJS 信息端點
//                                "/ws/**/*.js",     // SockJS JavaScript 文件
//                                "/ws/**/*.map",    // Source maps
//                                "/ws/*/eventsource",  // EventSource 傳輸
//                                "/ws/*/xhr",         // XHR 傳輸
//                                "/ws/*/xhr_send",    // XHR 發送
//                                "/ws/*/xhr_streaming", // XHR 流
//                                "/ws/*/websocket",     // WebSocket 傳輸
                                "/topic/**",
                                "/app/**",
                                "/user/**",
                                "/queue/**"
                        ).permitAll()
                        .anyRequest().authenticated()
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
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"" + authException.getMessage() + "\"}");
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));  // 允许所有来源
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
