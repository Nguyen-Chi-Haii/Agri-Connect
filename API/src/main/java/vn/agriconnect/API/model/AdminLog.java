package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * AdminLog Entity (Nhật ký hoạt động Admin)
 */
@Data
@Document(collection = "admin_logs")
public class AdminLog {
    @Id
    private String id;
    
    private String adminId;
    private String action;
    private String detail;
    
    private Instant timestamp;
}
