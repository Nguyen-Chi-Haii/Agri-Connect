package vn.agriconnect.API.dto.request.post;

import lombok.Data;
import vn.agriconnect.API.model.enums.PostStatus;

@Data
public class PostFilterRequest {
    private String keyword;
    private String categoryId;
    private String province;
    private String district;
    private Double minPrice;
    private Double maxPrice;
    private PostStatus status;
    private String sortBy;
    private String sortOrder;
    private Integer page = 0;
    private Integer size = 10;
}
