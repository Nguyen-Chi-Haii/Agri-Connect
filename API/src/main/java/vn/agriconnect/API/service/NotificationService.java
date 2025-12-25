package vn.agriconnect.API.service;

import vn.agriconnect.API.model.Notification;

import java.util.List;

public interface NotificationService {
    Notification create(String userId, String title, String content);
    List<Notification> getByUser(String userId);
    void markAsRead(String notificationId);
    void markAllAsRead(String userId);
    long countUnread(String userId);
}
