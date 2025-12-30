package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.request.feedback.FeedbackCreateRequest;
import vn.agriconnect.API.dto.request.feedback.FeedbackFilterRequest;
import vn.agriconnect.API.dto.response.PagedResponse;
import vn.agriconnect.API.exception.ResourceNotFoundException;
import vn.agriconnect.API.model.Feedback;
import vn.agriconnect.API.model.enums.FeedbackStatus;
import vn.agriconnect.API.repository.FeedbackRepository;
import vn.agriconnect.API.service.FeedbackService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Feedback create(String userId, FeedbackCreateRequest request) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setTargetUserId(request.getTargetUserId());
        feedback.setTitle(request.getTitle());
        feedback.setContent(request.getContent());
        feedback.setType(request.getType());
        feedback.setStatus(FeedbackStatus.NEW);
        
        // Chỉ set rating nếu đây là feedback cho user cụ thể
        if (request.getTargetUserId() != null && request.getRating() != null) {
            // Validate rating 1-5
            int rating = Math.max(1, Math.min(5, request.getRating()));
            feedback.setRating(rating);
        }
        
        return feedbackRepository.save(feedback);
    }

    @Override
    public Feedback getById(String feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback không tồn tại"));
    }

    @Override
    public List<Feedback> getByUser(String userId) {
        return feedbackRepository.findByUserId(userId);
    }

    @Override
    public List<Feedback> getByTargetUser(String targetUserId) {
        return feedbackRepository.findByTargetUserIdOrderByCreatedAtDesc(targetUserId);
    }

    @Override
    public Double getAverageRating(String targetUserId) {
        List<Feedback> feedbacks = feedbackRepository.findByTargetUserId(targetUserId);
        if (feedbacks.isEmpty()) {
            return null;
        }
        
        double sum = 0;
        int count = 0;
        for (Feedback fb : feedbacks) {
            if (fb.getRating() != null) {
                sum += fb.getRating();
                count++;
            }
        }
        
        return count > 0 ? sum / count : null;
    }

    @Override
    public List<Feedback> getAll() {
        return feedbackRepository.findAll();
    }

    @Override
    public PagedResponse<Feedback> search(FeedbackFilterRequest filter) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Filter by userId (người gửi)
        if (filter.getUserId() != null && !filter.getUserId().isEmpty()) {
            criteriaList.add(Criteria.where("userId").is(filter.getUserId()));
        }
        
        // Filter by targetUserId (người được đánh giá)
        if (filter.getTargetUserId() != null && !filter.getTargetUserId().isEmpty()) {
            criteriaList.add(Criteria.where("targetUserId").is(filter.getTargetUserId()));
        }
        
        // Filter by type
        if (filter.getType() != null) {
            criteriaList.add(Criteria.where("type").is(filter.getType()));
        }
        
        // Filter by status
        if (filter.getStatus() != null) {
            criteriaList.add(Criteria.where("status").is(filter.getStatus()));
        }
        
        // Filter by keyword (search in title and content)
        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            String regex = ".*" + filter.getKeyword() + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(regex, "i"),
                    Criteria.where("content").regex(regex, "i")
            ));
        }
        
        // Filter by rating range
        if (filter.getMinRating() != null) {
            criteriaList.add(Criteria.where("rating").gte(filter.getMinRating()));
        }
        if (filter.getMaxRating() != null) {
            criteriaList.add(Criteria.where("rating").lte(filter.getMaxRating()));
        }
        
        // Combine all criteria
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        // Count total
        long total = mongoTemplate.count(query, Feedback.class);
        
        // Pagination and sorting
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.skip((long) filter.getPage() * filter.getSize());
        query.limit(filter.getSize());
        
        List<Feedback> feedbacks = mongoTemplate.find(query, Feedback.class);
        
        int totalPages = (int) Math.ceil((double) total / filter.getSize());
        
        return new PagedResponse<>(
                feedbacks,
                filter.getPage(),
                filter.getSize(),
                total,
                totalPages,
                filter.getPage() == 0,
                filter.getPage() >= totalPages - 1
        );
    }

    @Override
    public Feedback updateStatus(String feedbackId, FeedbackStatus status) {
        Feedback feedback = getById(feedbackId);
        feedback.setStatus(status);
        return feedbackRepository.save(feedback);
    }

    @Override
    public void delete(String feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new ResourceNotFoundException("Feedback không tồn tại");
        }
        feedbackRepository.deleteById(feedbackId);
    }
}
