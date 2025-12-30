package vn.agriconnect.API.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.request.feedback.FeedbackCreateRequest;
import vn.agriconnect.API.dto.request.feedback.FeedbackFilterRequest;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.dto.response.PagedResponse;
import vn.agriconnect.API.model.Feedback;
import vn.agriconnect.API.model.enums.FeedbackStatus;
import vn.agriconnect.API.service.FeedbackService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feedback Controller
 * API quản lý góp ý và đánh giá giữa nông dân và thương lái
 */
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ==================== User Endpoints ====================

    /**
     * Tạo feedback mới (cho hệ thống hoặc đánh giá user khác)
     * POST /api/feedbacks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Feedback>> create(
            @Valid @RequestBody FeedbackCreateRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Feedback created = feedbackService.create(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Góp ý đã được gửi", created));
    }

    /**
     * Lấy feedback của người dùng hiện tại (feedback đã gửi)
     * GET /api/feedbacks/my-feedbacks
     */
    @GetMapping("/my-feedbacks")
    public ResponseEntity<ApiResponse<List<Feedback>>> getMyFeedbacks() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Feedback> feedbacks = feedbackService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    /**
     * Lấy feedback về người dùng hiện tại (được người khác đánh giá)
     * GET /api/feedbacks/about-me
     */
    @GetMapping("/about-me")
    public ResponseEntity<ApiResponse<List<Feedback>>> getFeedbacksAboutMe() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Feedback> feedbacks = feedbackService.getByTargetUser(userId);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    /**
     * Lấy điểm đánh giá trung bình của người dùng hiện tại
     * GET /api/feedbacks/my-rating
     */
    @GetMapping("/my-rating")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyRating() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Double avgRating = feedbackService.getAverageRating(userId);
        List<Feedback> feedbacks = feedbackService.getByTargetUser(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", avgRating);
        result.put("totalReviews", feedbacks.size());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== Public Endpoints ====================

    /**
     * Lấy feedback và rating của một user cụ thể (công khai)
     * GET /api/feedbacks/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserFeedback(
            @PathVariable String userId) {
        List<Feedback> feedbacks = feedbackService.getByTargetUser(userId);
        Double avgRating = feedbackService.getAverageRating(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("feedbacks", feedbacks);
        result.put("averageRating", avgRating);
        result.put("totalReviews", feedbacks.size());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== Search Endpoint ====================

    /**
     * Tìm kiếm feedback nâng cao
     * GET /api/feedbacks/search?userId=...&targetUserId=...&type=...&status=...&keyword=...
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<Feedback>>> search(
            FeedbackFilterRequest filter) {
        PagedResponse<Feedback> feedbacks = feedbackService.search(filter);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    // ==================== Admin Endpoints ====================

    /**
     * Lấy tất cả feedback (Admin only)
     * GET /api/feedbacks/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Feedback>>> getAll() {
        List<Feedback> feedbacks = feedbackService.getAll();
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    /**
     * Cập nhật trạng thái feedback (Admin only)
     * PUT /api/feedbacks/{feedbackId}/status?status=IN_PROGRESS|RESOLVED
     */
    @PutMapping("/{feedbackId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Feedback>> updateStatus(
            @PathVariable String feedbackId,
            @RequestParam FeedbackStatus status) {
        Feedback updated = feedbackService.updateStatus(feedbackId, status);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật trạng thái", updated));
    }

    /**
     * Xóa feedback (Admin only)
     * DELETE /api/feedbacks/{feedbackId}
     */
    @DeleteMapping("/{feedbackId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String feedbackId) {
        feedbackService.delete(feedbackId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa feedback", null));
    }

    /**
     * Lấy feedback theo ID
     * GET /api/feedbacks/{feedbackId}
     */
    @GetMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<Feedback>> getById(@PathVariable String feedbackId) {
        Feedback feedback = feedbackService.getById(feedbackId);
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }
}
