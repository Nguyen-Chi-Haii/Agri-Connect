package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * User API interface
 */
public interface UserApi {

    @GET("api/users/profile")
    Call<ApiResponse<UserProfile>> getProfile();

    @PUT("api/users/profile")
    Call<ApiResponse<UserProfile>> updateProfile(@Body UserProfile profile);

    @GET("api/users/{userId}")
    Call<ApiResponse<UserProfile>> getUserById(@Path("userId") String userId);

    @GET("api/statistics/summary")
    Call<ApiResponse<StatisticsResponse>> getStatistics();
}
