package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.agriconnect.API.model.enums.FeedbackStatus;
import vn.agriconnect.API.model.enums.FeedbackType;

import java.time.Instant;

/**
 * Feedback Entity (Góp ý từ người dùng)
 */
@Data
@Document(collection = "feedbacks")
public class Feedback {
    @Id
    private String id;
    
    private String userId;
    
    private String title;
    private String content;
    
    private FeedbackType type;
    private FeedbackStatus status = FeedbackStatus.NEW;
    
    @CreatedDate
    private Instant createdAt;
}
