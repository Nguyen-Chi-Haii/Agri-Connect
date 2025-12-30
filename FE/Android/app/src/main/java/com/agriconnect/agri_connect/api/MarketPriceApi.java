package com.agriconnect.agri_connect.api;

import com.agriconnect.agri_connect.api.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Market Price API interface
 */
public interface MarketPriceApi {
    
    @GET("api/market-prices")
    Call<ApiResponse<List<MarketPrice>>> getAllPrices();
    
    @GET("api/market-prices/category/{categoryId}")
    Call<ApiResponse<List<MarketPrice>>> getPricesByCategory(@Path("categoryId") String categoryId);
}
