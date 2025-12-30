package vn.agriconnect.API.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import vn.agriconnect.API.model.enums.MessageType;

@Data
public class SendMessageRequest {
    @NotBlank(message = "Conversation ID is required")
    private String conversationId;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private MessageType type = MessageType.TEXT;
    
    // Optional: Used for WebSocket messages when auth context is not available
    private String senderId;
}

