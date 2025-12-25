package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.model.Feedback;
import vn.agriconnect.API.service.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<Feedback>> create(@RequestBody Feedback feedback) {
        Feedback created = feedbackService.create(feedback);
        return ResponseEntity.ok(ApiResponse.success("Góp ý đã được gửi", created));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Feedback>>> getByUser(@PathVariable String userId) {
        List<Feedback> feedbacks = feedbackService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Feedback>>> getAll() {
        List<Feedback> feedbacks = feedbackService.getAll();
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }
}
