package vn.agriconnect.API.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.agriconnect.API.model.embedded.Location;
import vn.agriconnect.API.model.enums.PostStatus;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {
    private String id;
    private String title;
    private String description;
    private Double price;
    private String unit;
    private Double quantity;
    private List<String> images;

    private String categoryId;
    private String categoryName;

    private String sellerId;
    private String sellerName;
    private String sellerPhone;
    private String sellerAvatar;

    private Location location;
    private PostStatus status;
    private int viewCount;

    private Instant createdAt;

    // Interaction stats
    private int likeCount;
    private int commentCount;
    @com.fasterxml.jackson.annotation.JsonProperty("isLiked")
    private boolean isLiked;
}
