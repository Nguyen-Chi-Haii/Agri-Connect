package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryApi {
    @GET("api/categories")
    Call<ApiResponse<List<Category>>> getAllCategories();

    @POST("api/categories")
    Call<ApiResponse<Category>> createCategory(@Body Category category);

    @PUT("api/categories/{id}")
    Call<ApiResponse<Category>> updateCategory(@Path("id") String id, @Body Category category);

    @DELETE("api/categories/{id}")
    Call<ApiResponse<Void>> deleteCategory(@Path("id") String id);
}
