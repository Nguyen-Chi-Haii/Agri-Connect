package vn.agriconnect.API.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.agriconnect.API.model.Feedback;
import vn.agriconnect.API.model.enums.FeedbackStatus;
import vn.agriconnect.API.model.enums.FeedbackType;

import java.util.List;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    // Tìm feedback theo người gửi
    List<Feedback> findByUserId(String userId);
    
    // Tìm feedback theo người được đánh giá (targetUserId)
    List<Feedback> findByTargetUserId(String targetUserId);
    List<Feedback> findByTargetUserIdOrderByCreatedAtDesc(String targetUserId);
    
    // Tìm theo status
    List<Feedback> findByStatus(FeedbackStatus status);
    List<Feedback> findByStatusOrderByCreatedAtDesc(FeedbackStatus status);
    
    // Tìm theo type
    List<Feedback> findByType(FeedbackType type);
    
    // Tìm feedback cho user với rating >= minRating
    List<Feedback> findByTargetUserIdAndRatingGreaterThanEqual(String targetUserId, Integer minRating);
    
    // Đếm feedback của một user được đánh giá
    long countByTargetUserId(String targetUserId);
    
    // Tìm feedback của người gửi cho người nhận cụ thể
    List<Feedback> findByUserIdAndTargetUserId(String userId, String targetUserId);
}
