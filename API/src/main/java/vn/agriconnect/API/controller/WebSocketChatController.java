package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.agriconnect.API.dto.request.chat.SendMessageRequest;
import vn.agriconnect.API.model.Message;
import vn.agriconnect.API.service.ChatService;

/**
 * WebSocket Controller for Real-time Chat
 * Handles STOMP messages for instant messaging functionality
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle sending messages via WebSocket
     * Client sends to: /app/chat.send
     * Server broadcasts to: /queue/messages (to specific users)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Get sender ID from WebSocket session
            String senderId = headerAccessor.getUser() != null 
                    ? headerAccessor.getUser().getName() 
                    : request.getSenderId();
            
            if (senderId == null) {
                log.error("Cannot send message: senderId is null");
                return;
            }

            // Save message to database
            Message savedMessage = chatService.sendMessage(senderId, request);
            
            // Get conversation participants to send the message to
            // Send to conversation topic so all participants receive it
            String destination = "/topic/conversation." + request.getConversationId();
            messagingTemplate.convertAndSend(destination, savedMessage);
            
            log.info("Message sent from {} to conversation {}", senderId, request.getConversationId());
            
        } catch (Exception e) {
            log.error("Error sending message via WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingNotification notification) {
        String destination = "/topic/conversation." + notification.getConversationId() + ".typing";
        messagingTemplate.convertAndSend(destination, notification);
    }

    /**
     * Handle marking messages as read
     * Client sends to: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ReadNotification notification, SimpMessageHeaderAccessor headerAccessor) {
        String userId = headerAccessor.getUser() != null 
                ? headerAccessor.getUser().getName() 
                : notification.getUserId();
        
        if (userId != null) {
            chatService.markAsRead(notification.getConversationId(), userId);
            
            // Notify other users that messages have been read
            String destination = "/topic/conversation." + notification.getConversationId() + ".read";
            messagingTemplate.convertAndSend(destination, notification);
        }
    }

    // DTO classes for WebSocket payloads
    
    @lombok.Data
    public static class TypingNotification {
        private String conversationId;
        private String userId;
        private boolean isTyping;
    }

    @lombok.Data
    public static class ReadNotification {
        private String conversationId;
        private String userId;
    }
}
