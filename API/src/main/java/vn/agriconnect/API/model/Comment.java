package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    
    private String postId;
    private String userId; // User who commented
    private String content;
    
    @CreatedDate
    private Instant createdAt;
}
