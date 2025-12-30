package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Authentication API interface
 */
public interface AuthApi {
    
    @POST("api/auth/login")
    Call<ApiResponse<JwtResponse>> login(@Body LoginRequest request);
    
    @POST("api/auth/register")
    Call<ApiResponse<JwtResponse>> register(@Body RegisterRequest request);
    
    @POST("api/auth/refresh")
    Call<ApiResponse<JwtResponse>> refreshToken(@Header("Authorization") String refreshToken);
    
    @POST("api/auth/logout")
    Call<ApiResponse<Void>> logout(@Header("Authorization") String token);
}
