package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Post API interface
 */
public interface PostApi {
    
    @GET("api/posts/approved")
    Call<ApiResponse<List<Post>>> getApprovedPosts();
    
    @GET("api/posts/{postId}")
    Call<ApiResponse<Post>> getPostById(@Path("postId") String postId);
    
    @GET("api/posts/my-posts")
    Call<ApiResponse<List<Post>>> getMyPosts();
    
    @GET("api/posts/search")
    Call<ApiResponse<List<Post>>> searchPosts(
        @Query("keyword") String keyword,
        @Query("categoryId") String categoryId,
        @Query("minPrice") Double minPrice,
        @Query("maxPrice") Double maxPrice
    );
    
    @POST("api/posts")
    Call<ApiResponse<Post>> createPost(@Body Post post);
    
    @PUT("api/posts/{postId}")
    Call<ApiResponse<Post>> updatePost(@Path("postId") String postId, @Body Post post);
    
    @DELETE("api/posts/{postId}")
    Call<ApiResponse<Void>> deletePost(@Path("postId") String postId);
}
