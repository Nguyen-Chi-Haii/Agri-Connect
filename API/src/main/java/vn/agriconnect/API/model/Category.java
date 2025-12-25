package vn.agriconnect.API.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Category Entity
 */
@Data
@Document(collection = "categories")
public class Category {
    @Id
    private String id;
    
    private String name;
    private String icon;
    private String description;
    private String parentId; // null nếu là danh mục gốc, có giá trị nếu là danh mục con
}
