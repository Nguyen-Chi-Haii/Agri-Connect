package vn.agriconnect.API.model.embedded;

import lombok.Data;

/**
 * Product Context for Message - Embedded Document
 * Dùng khi gửi PRODUCT_CARD trong chat
 */
@Data
public class ProductContext {
    private String postId;
    private String name;
    private Double price;
    private String image;
}
