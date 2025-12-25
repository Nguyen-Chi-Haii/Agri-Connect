package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.agriconnect.API.model.enums.NotificationType;

import java.time.Instant;

/**
 * Notification Entity
 */
@Data
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    
    private String userId;
    
    private NotificationType type;
    private String title;
    private String content;
    
    private Boolean read = false; // Đổi từ isRead -> read để Lombok generate setRead()
    
    @CreatedDate
    private Instant createdAt;
}
