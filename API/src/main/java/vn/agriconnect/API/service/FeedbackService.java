package vn.agriconnect.API.service;

import vn.agriconnect.API.dto.request.feedback.FeedbackCreateRequest;
import vn.agriconnect.API.dto.request.feedback.FeedbackFilterRequest;
import vn.agriconnect.API.dto.response.PagedResponse;
import vn.agriconnect.API.model.Feedback;
import vn.agriconnect.API.model.enums.FeedbackStatus;

import java.util.List;

public interface FeedbackService {
    
    /**
     * Tạo feedback mới (hệ thống tự động lấy userId từ context)
     */
    Feedback create(String userId, FeedbackCreateRequest request);
    
    /**
     * Lấy feedback theo ID
     */
    Feedback getById(String feedbackId);
    
    /**
     * Lấy feedback theo người gửi
     */
    List<Feedback> getByUser(String userId);
    
    /**
     * Lấy feedback về một user cụ thể (người được đánh giá)
     */
    List<Feedback> getByTargetUser(String targetUserId);
    
    /**
     * Lấy điểm đánh giá trung bình của một user
     */
    Double getAverageRating(String targetUserId);
    
    /**
     * Lấy tất cả feedback (Admin only)
     */
    List<Feedback> getAll();
    
    /**
     * Tìm kiếm feedback nâng cao
     */
    PagedResponse<Feedback> search(FeedbackFilterRequest filter);
    
    /**
     * Cập nhật trạng thái feedback (Admin only)
     */
    Feedback updateStatus(String feedbackId, FeedbackStatus status);
    
    /**
     * Xóa feedback (Admin only)
     */
    void delete(String feedbackId);
}
