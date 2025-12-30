package vn.agriconnect.API.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration
 * - Real-time chat functionality
 * - STOMP message broker for pub/sub messaging
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure STOMP endpoints for WebSocket connections
     * Client connects to: ws://localhost:8080/ws
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Allow all origins (configure properly for production)
                .withSockJS();  // SockJS fallback for browsers that don't support WebSocket
        
        // Native WebSocket endpoint (without SockJS) for mobile clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Configure message broker for routing messages
     * - /topic: broadcast to all subscribers (e.g., notifications)
     * - /queue: point-to-point messaging (e.g., private chat)
     * - /app: prefix for messages sent from client to server
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        // Enable simple in-memory message broker
        // Messages with destination /topic/* will be broadcast to all subscribers
        // Messages with destination /queue/* will be sent to specific users
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages sent from client to server
        // Client sends to /app/chat.send, server receives at @MessageMapping("/chat.send")
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        // Server sends to /user/{userId}/queue/messages
        registry.setUserDestinationPrefix("/user");
    }
}
