package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * RefreshToken Entity
 */
@Data
@Document(collection = "refresh_tokens")
public class RefreshToken {
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed(unique = true)
    private String token;
    
    private Instant expiresAt;
    private boolean isRevoked = false;
}
