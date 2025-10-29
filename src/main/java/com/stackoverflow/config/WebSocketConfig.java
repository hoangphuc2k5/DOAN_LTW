package com.stackoverflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for real-time messaging
 * 
 * Endpoints:
 * - /ws: SockJS endpoint for client connections
 * - /app: Destination prefix for sending messages to server
 * - /topic: Broadcast messages to all subscribers
 * - /queue: Private messages to specific users
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
        
        // Set prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins (configure properly in production)
                .withSockJS();
        
        // Also add without SockJS for native WebSocket support
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}