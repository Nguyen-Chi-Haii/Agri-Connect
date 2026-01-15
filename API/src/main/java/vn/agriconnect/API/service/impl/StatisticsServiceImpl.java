package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.response.StatisticsResponse;
import vn.agriconnect.API.model.enums.PostStatus;
import vn.agriconnect.API.repository.ConversationRepository;
import vn.agriconnect.API.repository.PostRepository;
import vn.agriconnect.API.service.StatisticsService;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PostRepository postRepository;
    private final ConversationRepository conversationRepository;
    private final vn.agriconnect.API.repository.CategoryRepository categoryRepository;

    @Override
    public StatisticsResponse getUserStatistics(String userId) {
        long totalPosts = postRepository.countBySellerId(userId);
        long approvedPosts = postRepository.countBySellerIdAndStatus(userId, PostStatus.APPROVED);
        long pendingPosts = postRepository.countBySellerIdAndStatus(userId, PostStatus.PENDING);
        long rejectedPosts = postRepository.countBySellerIdAndStatus(userId, PostStatus.REJECTED);

        long totalInteractions = conversationRepository.findByParticipantsContaining(userId).size();

        // Calculate Category Stats (Global Market Distribution)
        // Note: Ideally this should be cached or separated, but for this feature requirement
        // we will return it here or creates a separates methods.
        // The requirement says "StatisticsActivity to show Category Pie Chart".
        // Assuming this is "My Posts Distribution" if it's user stats.
        // Or "Global Market" if it's general.
        // Let's implement User's Post Distribution per Category.
        
        // Wait, looping all categories and counting USER posts per category is better.
        // postRepository.countBySellerIdAndCategoryId? No, we need "findAllCategory", then "countBySellerIdAndCategory".
        
        java.util.List<StatisticsResponse.CategoryStat> categoryStats = new java.util.ArrayList<>();
        java.util.List<vn.agriconnect.API.model.Category> categories = categoryRepository.findAll();
        
        for (vn.agriconnect.API.model.Category cat : categories) {
             // For User Statistics, we want specific user's posts distribution.
             // If we want GLOBAL, we use countByCategoryId(cat.getId())
             // Given "Market Statistics" usually means Global. But this is "getUserStatistics".
             // Let's assume the user wants to see the Global Market Demand to decide what to sell.
             // So Global Stats is more useful for "Market Statistics".
             
             long count = postRepository.countByCategoryId(cat.getId());
             if (count > 0) {
                 categoryStats.add(StatisticsResponse.CategoryStat.builder()
                     .categoryId(cat.getId())
                     .categoryName(cat.getName())
                     .postCount(count)
                     .build());
             }
        }
        // Sort by count desc
        categoryStats.sort((a, b) -> Long.compare(b.getPostCount(), a.getPostCount()));

        return StatisticsResponse.builder()
                .totalPosts(totalPosts)
                .approvedPosts(approvedPosts)
                .pendingPosts(pendingPosts)
                .rejectedPosts(rejectedPosts)
                .totalInteractions(totalInteractions)
                .categoryStats(categoryStats)
                .build();
    }
}
