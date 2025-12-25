package vn.agriconnect.API.service;

import vn.agriconnect.API.model.Feedback;

import java.util.List;

public interface FeedbackService {
    Feedback create(Feedback feedback);
    List<Feedback> getByUser(String userId);
    List<Feedback> getAll();
}
