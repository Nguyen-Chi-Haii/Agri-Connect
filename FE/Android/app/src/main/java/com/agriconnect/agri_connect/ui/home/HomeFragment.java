package com.agriconnect.agri_connect.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.PostApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Post;
import com.agriconnect.agri_connect.ui.post.CreatePostActivity;
import com.agriconnect.agri_connect.ui.search.SearchActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvPosts;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreatePost;
    private ImageView btnSearch;
    private PostAdapter postAdapter;
    private PostApi postApi;

    private final ActivityResultLauncher<Intent> createPostLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    // Refresh posts
                    loadPosts();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize API
        if (getContext() != null) {
            postApi = ApiClient.getInstance(getContext()).getPostApi();
        }
        
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadPosts();
    }

    private void initViews(View view) {
        rvPosts = view.findViewById(R.id.rvPosts);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        fabCreatePost = view.findViewById(R.id.fabCreatePost);
        btnSearch = view.findViewById(R.id.btnSearch);
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter();
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(postAdapter);
    }

    private void setupListeners() {
        fabCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreatePostActivity.class);
            createPostLauncher.launch(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        
        postApi.getApprovedPosts().enqueue(new Callback<ApiResponse<List<Post>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Post>>> call, Response<ApiResponse<List<Post>>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Post> posts = response.body().getData();
                    
                    if (posts != null && !posts.isEmpty()) {
                        // Convert to PostItem for adapter
                        List<PostItem> postItems = new ArrayList<>();
                        for (Post post : posts) {
                            PostItem item = new PostItem(
                                post.getId(),
                                post.getSellerName(),
                                formatTime(post.getCreatedAt()),
                                post.getDescription() != null ? post.getDescription() : post.getTitle(),
                                formatPrice(post.getPrice(), post.getUnit()),
                                0, // likeCount
                                0, // commentCount
                                post.getViewCount(),
                                post.isSellerVerified()
                            );
                            if (post.getImages() != null && !post.getImages().isEmpty()) {
                                item.imageUrl = post.getImages().get(0);
                            }
                            postItems.add(item);
                        }
                        
                        postAdapter.setData(postItems);
                        rvPosts.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                    } else {
                        showEmpty();
                    }
                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Post>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmpty();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvPosts.setVisibility(View.GONE);
    }
    
    private String formatTime(String createdAt) {
        // Simple format - in real app, use proper date parsing
        if (createdAt == null) return "";
        return createdAt.substring(0, Math.min(10, createdAt.length()));
    }
    
    private String formatPrice(Double price, String unit) {
        if (price == null) return null;
        String priceStr = String.format("%,.0f", price) + "đ";
        if (unit != null) {
            priceStr += "/" + unit;
        }
        return priceStr;
    }
    
    // Post data class
    public static class PostItem {
        public String id;
        public String userName;
        public String time;
        public String content;
        public String price;
        public int likeCount;
        public int commentCount;
        public int viewCount;
        public boolean isVerified;
        public String imageUrl;
        
        public PostItem(String id, String userName, String time, String content, 
                       String price, int likeCount, int commentCount, int viewCount, boolean isVerified) {
            this.id = id;
            this.userName = userName;
            this.time = time;
            this.content = content;
            this.price = price;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.viewCount = viewCount;
            this.isVerified = isVerified;
        }
    }
}
