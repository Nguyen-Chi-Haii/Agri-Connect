package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * OTP Entity - Lưu mã OTP xác thực số điện thoại
 * Collection tự động tạo khi app chạy (Code-First)
 */
@Data
@Document(collection = "otps")
public class Otp {
    @Id
    private String id;
    
    @Indexed
    private String phone;
    
    private String code;
    
    private Instant createdAt;
    
    @Indexed(expireAfter = "0s") // TTL Index - MongoDB tự động xóa khi hết hạn
    private Instant expiresAt;
    
    private boolean used = false;
}
