package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.agriconnect.API.model.embedded.ProductContext;
import vn.agriconnect.API.model.enums.MessageType;

import java.time.Instant;
import java.util.List;

/**
 * Message Entity
 */
@Data
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    
    private String conversationId;
    private String senderId;
    
    private MessageType type = MessageType.TEXT;
    private String content;
    private List<String> images;
    
    private ProductContext productContext; // Khi type = PRODUCT_CARD
    
    private boolean isRead = false;
    
    @CreatedDate
    private Instant createdAt;
}
