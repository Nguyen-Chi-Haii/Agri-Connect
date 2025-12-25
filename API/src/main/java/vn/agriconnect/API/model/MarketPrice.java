package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * MarketPrice Entity (Lịch sử giá thị trường)
 */
@Data
@Document(collection = "market_prices")
public class MarketPrice {
    @Id
    private String id;
    
    private String categoryId;
    
    private LocalDate date;
    
    private Double avgPrice;
    private Double minPrice;
    private Double maxPrice;
    
    private int postCount; // Số lượng bài đăng mẫu
}
