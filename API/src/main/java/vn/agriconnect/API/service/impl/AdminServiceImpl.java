package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.model.AdminLog;
import vn.agriconnect.API.model.enums.PostStatus;
import vn.agriconnect.API.repository.*;
import vn.agriconnect.API.service.AdminService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminLogRepository adminLogRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    public void logAction(String adminId, String action, String detail) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction(action);
        log.setDetail(detail);
        log.setTimestamp(Instant.now());
        adminLogRepository.save(log);
    }

    @Override
    public List<AdminLog> getLogs(String adminId) {
        return adminLogRepository.findByAdminIdOrderByTimestampDesc(adminId);
    }

    @Override
    public List<AdminLog> getAllLogs() {
        return adminLogRepository.findAllByOrderByTimestampDesc();
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // User statistics
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // Post statistics
        long totalPosts = postRepository.count();
        long pendingPosts = postRepository.findByStatus(PostStatus.PENDING).size();
        long approvedPosts = postRepository.findByStatus(PostStatus.APPROVED).size();
        long rejectedPosts = postRepository.findByStatus(PostStatus.REJECTED).size();
        
        stats.put("totalPosts", totalPosts);
        stats.put("pendingPosts", pendingPosts);
        stats.put("approvedPosts", approvedPosts);
        stats.put("rejectedPosts", rejectedPosts);
        
        // Message statistics
        long totalMessages = messageRepository.count();
        stats.put("totalMessages", totalMessages);
        
        // Conversation statistics
        long totalConversations = conversationRepository.count();
        stats.put("totalConversations", totalConversations);
        
        // Feedback statistics
        long totalFeedbacks = feedbackRepository.count();
        stats.put("totalFeedbacks", totalFeedbacks);
        
        return stats;
    }
}
