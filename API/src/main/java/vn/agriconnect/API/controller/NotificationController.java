package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.model.Notification;
import vn.agriconnect.API.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications() {
        // TODO: Get current user ID from SecurityContext
        List<Notification> notifications = notificationService.getByUser("currentUserId");
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        // TODO: Get current user ID from SecurityContext
        long count = notificationService.countUnread("currentUserId");
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        // TODO: Get current user ID from SecurityContext
        notificationService.markAllAsRead("currentUserId");
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }
}
