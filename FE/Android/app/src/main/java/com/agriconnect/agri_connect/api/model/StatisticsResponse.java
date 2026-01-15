package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Statistics response model
 */
public class StatisticsResponse {
    @SerializedName("totalPosts")
    private long totalPosts;

    @SerializedName("approvedPosts")
    private long approvedPosts;

    @SerializedName("pendingPosts")
    private long pendingPosts;

    @SerializedName("rejectedPosts")
    private long rejectedPosts;

    @SerializedName("totalInteractions")
    private long totalInteractions;

    // Getters
    public long getTotalPosts() {
        return totalPosts;
    }

    public long getApprovedPosts() {
        return approvedPosts;
    }

    public long getPendingPosts() {
        return pendingPosts;
    }

    public long getRejectedPosts() {
        return rejectedPosts;
    }

    public long getTotalInteractions() {
        return totalInteractions;
    }

    @SerializedName("categoryStats")
    private java.util.List<CategoryStat> categoryStats;

    public java.util.List<CategoryStat> getCategoryStats() {
        return categoryStats;
    }

    public static class CategoryStat {
        @SerializedName("categoryId")
        public String categoryId;
        @SerializedName("categoryName")
        public String categoryName;
        @SerializedName("postCount")
        public long postCount;
    }
}
