package vn.agriconnect.API.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.agriconnect.API.model.Feedback;
import vn.agriconnect.API.model.enums.FeedbackStatus;

import java.util.List;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    List<Feedback> findByUserId(String userId);
    List<Feedback> findByStatus(FeedbackStatus status);
    List<Feedback> findByStatusOrderByCreatedAtDesc(FeedbackStatus status);
}
