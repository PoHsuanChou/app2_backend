package org.example.config;

import org.example.interceptor.CustomHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 開發環境可以允許所有來源
                .addInterceptors(new CustomHandshakeInterceptor())
                .setHandshakeHandler(new DefaultHandshakeHandler()) // 添加默認握手處理器
                .withSockJS()  // 啟用 SockJS
                .setStreamBytesLimit(512 * 1024)  // 設置流限制
                .setHttpMessageCacheSize(1000)    // 設置消息緩存大小
                .setDisconnectDelay(30 * 1000);   // 設置斷開延遲
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
    }
}
