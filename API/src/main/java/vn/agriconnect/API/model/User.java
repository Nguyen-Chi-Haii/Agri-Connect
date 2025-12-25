package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.agriconnect.API.model.embedded.KycInfo;
import vn.agriconnect.API.model.enums.Role;

import java.time.Instant;

/**
 * User Entity
 */
@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String phone;
    
    private String password;
    private String fullName;
    private String avatar;
    private String address;
    
    private Role role;
    private boolean isActive = true;
    
    private KycInfo kyc;
    
    @CreatedDate
    private Instant createdAt;
}
