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

    @Override
    public StatisticsResponse getUserStatistics(String userId) {
        long totalPosts = postRepository.countBySellerId(userId);
        long approvedPosts = postRepository.countBySellerIdAndStatus(userId, PostStatus.APPROVED);
        long pendingPosts = postRepository.countBySellerIdAndStatus(userId, PostStatus.PENDING);
        long rejectedPosts = postRepository.countBySellerIdAndStatus(userId, PostStatus.REJECTED);

        long totalInteractions = conversationRepository.findByParticipantsContaining(userId).size();

        return StatisticsResponse.builder()
                .totalPosts(totalPosts)
                .approvedPosts(approvedPosts)
                .pendingPosts(pendingPosts)
                .rejectedPosts(rejectedPosts)
                .totalInteractions(totalInteractions)
                .build();
    }
}
