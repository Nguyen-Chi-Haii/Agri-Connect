package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Notification;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationApi {

    @GET("api/notifications")
    Call<ApiResponse<List<Notification>>> getNotifications();

    @GET("api/notifications/unread-count")
    Call<ApiResponse<Long>> getUnreadCount();

    @PUT("api/notifications/{id}/read")
    Call<ApiResponse<Void>> markAsRead(@Path("id") String id);

    @PUT("api/notifications/read-all")
    Call<ApiResponse<Void>> markAllAsRead();
}
