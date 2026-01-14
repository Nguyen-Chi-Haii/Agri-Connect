package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Admin API interface for admin operations
 */
public interface AdminApi {

    @GET("api/admin/dashboard")
    Call<ApiResponse<Map<String, Object>>> getDashboardStats();

    @GET("api/admin/logs")
    Call<ApiResponse<List<AdminLog>>> getAdminLogs();

    @GET("api/users")
    Call<ApiResponse<List<UserProfile>>> getAllUsers();

    @GET("api/posts")
    Call<ApiResponse<PagedResponse<Post>>> getAllPosts(
            @Query("status") String status,
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("api/posts/{postId}/approve")
    Call<ApiResponse<Void>> approvePost(@Path("postId") String postId);

    @PUT("api/posts/{postId}/reject")
    Call<ApiResponse<Void>> rejectPost(@Path("postId") String postId, @Query("reason") String reason);

    @PUT("api/posts/{postId}/close")
    Call<ApiResponse<Void>> closePost(@Path("postId") String postId);

    @PUT("api/users/{userId}/kyc/verify")
    Call<ApiResponse<Void>> verifyKyc(@Path("userId") String userId);

    @PUT("api/users/{userId}/kyc/reject")
    Call<ApiResponse<Void>> rejectKyc(@Path("userId") String userId, @Query("reason") String reason);
}
