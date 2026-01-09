package vn.agriconnect.API.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsResponse {
    private long totalPosts;
    private long approvedPosts;
    private long pendingPosts;
    private long rejectedPosts;
    private long totalInteractions; // Number of distinct chat partners
}
