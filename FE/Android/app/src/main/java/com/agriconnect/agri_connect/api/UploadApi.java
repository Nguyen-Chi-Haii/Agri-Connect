package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.ApiResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UploadApi {

    @Multipart
    @POST("api/upload")
    Call<ApiResponse<Map<String, Object>>> uploadFile(
            @Part MultipartBody.Part file,
            @Query("folder") String folder
    );
}
