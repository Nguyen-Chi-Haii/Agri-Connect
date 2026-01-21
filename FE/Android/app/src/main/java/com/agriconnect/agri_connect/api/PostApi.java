package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.*;
import java.util.List;
import java.util.Map;
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
    Call<ApiResponse<List<Post>>> getMyPosts(@Query("status") String status);

    @GET("api/posts")
    Call<ApiResponse<PagedResponse<Post>>> searchPosts(
            @Query("keyword") String keyword,
            @Query("categoryId") String categoryId,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice);

    @POST("api/posts")
    Call<ApiResponse<Post>> createPost(@Body Post post);

    @PUT("api/posts/{postId}")
    Call<ApiResponse<Post>> updatePost(@Path("postId") String postId, @Body Post post);

    @DELETE("api/posts/{postId}")
    Call<ApiResponse<Void>> deletePost(@Path("postId") String postId);

    @PUT("api/posts/{postId}/close")
    Call<ApiResponse<Void>> closePost(@Path("postId") String postId);

    // Like API
    @POST("api/posts/{postId}/like")
    Call<ApiResponse<Void>> toggleLike(@Path("postId") String postId);

    // Comment APIs
    @GET("api/posts/{postId}/comments")
    Call<ApiResponse<PagedResponse<Comment>>> getComments(
            @Path("postId") String postId,
            @Query("page") int page,
            @Query("size") int size);

    @POST("api/posts/{postId}/comments")
    Call<ApiResponse<Comment>> addComment(
            @Path("postId") String postId,
            @Body Map<String, String> body);
}
