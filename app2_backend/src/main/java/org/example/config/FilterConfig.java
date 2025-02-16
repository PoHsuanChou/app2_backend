package org.example.config;

import org.example.filter.JwtAuthenticationFilter;
import org.example.filter.RequestResponseLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestResponseLoggingFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
//不需要！實際上，如果你已經在 SecurityFilterChain 中使用 addFilterBefore 添加了 JwtAuthenticationFilter，
// 就不應該再使用 FilterRegistrationBean 註冊它。
//    @Bean
//    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
//            JwtAuthenticationFilter filter) {
//        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(filter);
//        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
//        return registration;
//    }
}



