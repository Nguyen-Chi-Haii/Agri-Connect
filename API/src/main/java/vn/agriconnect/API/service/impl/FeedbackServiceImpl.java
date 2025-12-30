package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.model.Feedback;
import vn.agriconnect.API.repository.FeedbackRepository;
import vn.agriconnect.API.service.FeedbackService;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Override
    public Feedback create(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    @Override
    public List<Feedback> getByUser(String userId) {
        return feedbackRepository.findByUserId(userId);
    }

    @Override
    public List<Feedback> getAll() {
        return feedbackRepository.findAll();
    }
}
