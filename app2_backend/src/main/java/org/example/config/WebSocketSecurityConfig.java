package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    log.debug("Processing CONNECT message");
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        log.debug("Found Bearer token");

                        try {
                            String userId = jwtService.extractUserId(token);
                            log.debug("Extracted user ID: {}", userId);

                            User user = userRepository.findById(userId).orElse(null);
                            if (user != null && jwtService.isTokenValid(token, user)) {
                                log.debug("User authenticated: {}", userId);

                                // Simplified authentication
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

                                accessor.setUser(auth);
                                log.debug("Set authentication in accessor");
                            } else {
                                log.warn("User not found or token invalid");
                            }
                        } catch (Exception e) {
                            log.error("Error processing token", e);
                        }
                    } else {
                        log.warn("No Authorization header found in CONNECT frame");
                    }
                }

                return message;
            }
        });
    }
}