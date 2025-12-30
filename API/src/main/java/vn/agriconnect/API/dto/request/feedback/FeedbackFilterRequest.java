package vn.agriconnect.API.dto.request.feedback;

import lombok.Data;
import vn.agriconnect.API.model.enums.FeedbackStatus;
import vn.agriconnect.API.model.enums.FeedbackType;

/**
 * Request DTO để tìm kiếm/lọc feedback
 */
@Data
public class FeedbackFilterRequest {
    
    /**
     * ID người gửi feedback
     */
    private String userId;
    
    /**
     * ID người được đánh giá
     */
    private String targetUserId;
    
    /**
     * Loại feedback
     */
    private FeedbackType type;
    
    /**
     * Trạng thái feedback
     */
    private FeedbackStatus status;
    
    /**
     * Từ khóa tìm kiếm (trong title hoặc content)
     */
    private String keyword;
    
    /**
     * Rating tối thiểu
     */
    private Integer minRating;
    
    /**
     * Rating tối đa
     */
    private Integer maxRating;
    
    /**
     * Số trang (0-indexed)
     */
    private int page = 0;
    
    /**
     * Số lượng mỗi trang
     */
    private int size = 10;
}
