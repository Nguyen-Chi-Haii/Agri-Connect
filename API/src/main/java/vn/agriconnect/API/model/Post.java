package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.agriconnect.API.model.embedded.Location;
import vn.agriconnect.API.model.enums.PostStatus;

import java.time.Instant;
import java.util.List;

/**
 * Post Entity (Bài đăng)
 * Đây là nơi chứa các bài đăng của người dùng (nông sản, vật tư...)
 */
@Data
@Document(collection = "posts")
public class Post {
    @Id
    private String id;

    private String sellerId;
    private String categoryId;

    private String title;
    private String description;
    private List<String> images;

    private Double price;
    private String unit;
    private Double quantity;

    private Location location;

    private PostStatus status = PostStatus.PENDING;
    private int viewCount = 0;

    @CreatedDate
    private Instant createdAt;

    // New fields for interaction
    private java.util.List<String> likedUserIds = new java.util.ArrayList<>();
    private int commentCount = 0;
}
