package com.agriconnect.agri_connect.api;

import android.content.Context;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Singleton API Client using Retrofit
 */
public class ApiClient {
    // TODO: Change this to your actual API URL
    // For local development on emulator: 10.0.2.2
    // For local development on device: your PC's IP address
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    
    private static ApiClient instance;
    private final Retrofit retrofit;
    private final TokenManager tokenManager;

    // API interfaces
    private AuthApi authApi;
    private UserApi userApi;
    private PostApi postApi;
    private MarketPriceApi marketPriceApi;
    private ChatApi chatApi;

    private ApiClient(Context context) {
        tokenManager = TokenManager.getInstance(context);
        
        // Logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Auth interceptor to add token to requests
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            String authHeader = tokenManager.getAuthHeader();
            
            if (authHeader != null && !original.url().encodedPath().contains("/auth/")) {
                Request.Builder builder = original.newBuilder()
                    .header("Authorization", authHeader);
                return chain.proceed(builder.build());
            }
            return chain.proceed(original);
        };
        
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        
        retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }
    
    public static void resetInstance() {
        instance = null;
    }

    public AuthApi getAuthApi() {
        if (authApi == null) {
            authApi = retrofit.create(AuthApi.class);
        }
        return authApi;
    }

    public UserApi getUserApi() {
        if (userApi == null) {
            userApi = retrofit.create(UserApi.class);
        }
        return userApi;
    }

    public PostApi getPostApi() {
        if (postApi == null) {
            postApi = retrofit.create(PostApi.class);
        }
        return postApi;
    }

    public MarketPriceApi getMarketPriceApi() {
        if (marketPriceApi == null) {
            marketPriceApi = retrofit.create(MarketPriceApi.class);
        }
        return marketPriceApi;
    }

    public ChatApi getChatApi() {
        if (chatApi == null) {
            chatApi = retrofit.create(ChatApi.class);
        }
        return chatApi;
    }
    
    public TokenManager getTokenManager() {
        return tokenManager;
    }
}
