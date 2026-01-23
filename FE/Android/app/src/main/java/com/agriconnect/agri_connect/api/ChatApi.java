package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Chat API interface
 */
public interface ChatApi {

    @GET("api/chat/conversations")
    Call<ApiResponse<List<Conversation>>> getConversations();

    @POST("api/chat/conversations/{otherUserId}")
    Call<ApiResponse<Conversation>> createConversation(@Path("otherUserId") String otherUserId);

    @PUT("api/chat/conversations/{conversationId}/read")
    Call<ApiResponse<Void>> markAsRead(@Path("conversationId") String conversationId);

    @GET("api/chat/conversations/{conversationId}/messages")
    Call<ApiResponse<List<Message>>> getMessages(@Path("conversationId") String conversationId);

    @POST("api/chat/messages")
    Call<ApiResponse<Message>> sendMessage(@Body SendMessageRequest request);

    @GET("api/chat/unread-count")
    Call<ApiResponse<Long>> getUnreadCount();
}
