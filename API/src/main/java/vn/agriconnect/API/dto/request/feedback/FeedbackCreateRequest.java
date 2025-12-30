package vn.agriconnect.API.dto.request.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.agriconnect.API.model.enums.FeedbackType;

/**
 * Request DTO để tạo feedback cho một user cụ thể
 */
@Data
public class FeedbackCreateRequest {
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;
    
    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 2000, message = "Nội dung tối đa 2000 ký tự")
    private String content;
    
    @NotNull(message = "Loại feedback không được để trống")
    private FeedbackType type;
    
    /**
     * ID của user được đánh giá (thương lái hoặc nông dân)
     * Nếu null, đây là feedback cho hệ thống
     */
    private String targetUserId;
    
    /**
     * Đánh giá sao (1-5), chỉ áp dụng khi targetUserId không null
     */
    private Integer rating;
}
