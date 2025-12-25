package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.agriconnect.API.model.embedded.LastMessage;

import java.time.Instant;
import java.util.List;

/**
 * Conversation Entity (Cuộc hội thoại 1-1)
 */
@Data
@Document(collection = "conversations")
public class Conversation {
    @Id
    private String id;
    
    private List<String> participants; // [UserA_id, UserB_id]
    
    private LastMessage lastMessage;
    
    @LastModifiedDate
    private Instant updatedAt;
}
