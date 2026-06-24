// ============================================================
// WebSocketConfig.java
// Package: com.f3school.config
// ============================================================
package com.golearn.myf3school_backend.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple in-memory broker for personal queues + topic broadcasts
        config.enableSimpleBroker("/queue", "/topic");

        // Prefix for messages FROM client → server (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific queues (/user/{id}/queue/...)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws-chat")          // SockJS fallback
                .setAllowedOriginPatterns("*")    // tighten in production
                .withSockJS();
    }
}