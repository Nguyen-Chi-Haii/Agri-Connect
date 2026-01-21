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
import com.agriconnect.agri_connect.api.CategoryApi;
import com.agriconnect.agri_connect.api.PostApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Category;
import com.agriconnect.agri_connect.api.model.PagedResponse;
import com.agriconnect.agri_connect.api.model.Post;
import com.agriconnect.agri_connect.ui.post.CreatePostActivity;
import com.agriconnect.agri_connect.ui.post.PostDetailActivity;
import com.agriconnect.agri_connect.ui.search.SearchActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvPosts, rvCategories;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreatePost;
    private ImageView btnSearch;

    private PostAdapter postAdapter;
    private HomeCategoryAdapter categoryAdapter;

    private PostApi postApi;
    private CategoryApi categoryApi;
    private com.agriconnect.agri_connect.api.UserApi userApi;

    // Filter state
    private String currentCategoryId = null;

    private final ActivityResultLauncher<Intent> createPostLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    // Refresh posts
                    loadPosts(currentCategoryId);
                }
            });

    private final ActivityResultLauncher<Intent> postDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    // Update post state from detail view
                    Intent data = result.getData();
                    String postId = data.getStringExtra("postId");
                    boolean isLiked = data.getBooleanExtra("isLiked", false);
                    int likeCount = data.getIntExtra("likeCount", 0);
                    int commentCount = data.getIntExtra("commentCount", 0);
                    
                    if (postId != null) {
                        postAdapter.updatePostState(postId, isLiked, likeCount, commentCount);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API
        if (getContext() != null) {
            ApiClient client = ApiClient.getInstance(getContext());
            postApi = client.getPostApi();
            categoryApi = client.getCategoryApi();
            userApi = client.getUserApi();
        }

        initViews(view);
        setupRecyclerView();
        setupListeners();

        loadCategories();
        loadPosts(null);
    }

    private void initViews(View view) {
        rvPosts = view.findViewById(R.id.rvPosts);
        rvCategories = view.findViewById(R.id.rvCategories);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        fabCreatePost = view.findViewById(R.id.fabCreatePost);
        btnSearch = view.findViewById(R.id.btnSearch);
    }

    private void setupRecyclerView() {
        // Posts
        postAdapter = new PostAdapter();
        postAdapter.setOnPostClickListener(postId -> {
            Intent intent = new Intent(getContext(), PostDetailActivity.class);
            intent.putExtra("postId", postId);
            postDetailLauncher.launch(intent);
        });
        postAdapter.setOnLikeClickListener((postId, position) -> {
            toggleLike(postId, position);
        });
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(postAdapter);

        // Categories
        categoryAdapter = new HomeCategoryAdapter();
        categoryAdapter.setOnCategoryClickListener(category -> {
            currentCategoryId = category != null ? category.getId() : null;
            loadPosts(currentCategoryId);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupListeners() {
        fabCreatePost.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            fabCreatePost.setEnabled(false);

            userApi.getProfile()
                    .enqueue(new Callback<ApiResponse<com.agriconnect.agri_connect.api.model.UserProfile>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<com.agriconnect.agri_connect.api.model.UserProfile>> call,
                                Response<ApiResponse<com.agriconnect.agri_connect.api.model.UserProfile>> response) {
                            progressBar.setVisibility(View.GONE);
                            fabCreatePost.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                com.agriconnect.agri_connect.api.model.UserProfile user = response.body().getData();
                                if (user != null && user.getKyc() != null
                                        && "VERIFIED".equals(user.getKyc().getStatus())) {
                                    Intent intent = new Intent(getContext(), CreatePostActivity.class);
                                    createPostLauncher.launch(intent);
                                } else {
                                    // Determine message based on role or just generic
                                    String msg = "Bạn cần xác minh danh tính để đăng bài.";
                                    if (user != null && user.getKyc() != null
                                            && "PENDING".equals(user.getKyc().getStatus())) {
                                        msg += "\nHồ sơ của bạn đang chờ duyệt.";
                                    } else {
                                        msg += "\nVui lòng cập nhật mã số thuế (Thương lái) hoặc CCCD (Nông dân).";
                                    }

                                    new android.app.AlertDialog.Builder(getContext())
                                            .setTitle("Yêu cầu xác minh")
                                            .setMessage(msg)
                                            .setPositiveButton("Xác minh ngay", (dialog, which) -> {
                                                Intent intent = new Intent(getContext(),
                                                        com.agriconnect.agri_connect.ui.profile.EditProfileActivity.class);
                                                startActivity(intent);
                                            })
                                            .setNegativeButton("Để sau", null)
                                            .show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Không thể kiểm tra trạng thái tài khoản",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<com.agriconnect.agri_connect.api.model.UserProfile>> call,
                                Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            fabCreatePost.setEnabled(true);
                            Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
    }

    private void loadCategories() {
        categoryApi.getAllCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call,
                    Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryAdapter.setCategories(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                // Silently fail for categories, just don't show them
            }
        });
    }

    private void loadPosts(String categoryId) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvPosts.setVisibility(View.GONE);

        if (categoryId == null) {
            // Load All Approved Posts
            postApi.getApprovedPosts().enqueue(new Callback<ApiResponse<List<Post>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Post>>> call, Response<ApiResponse<List<Post>>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        displayPosts(response.body().getData());
                    } else {
                        showEmpty();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Post>>> call, Throwable t) {
                    handleError(t);
                }
            });
        } else {
            // Filter by Category
            postApi.searchPosts(null, categoryId, null, null).enqueue(new Callback<ApiResponse<PagedResponse<Post>>>() {
                @Override
                public void onResponse(Call<ApiResponse<PagedResponse<Post>>> call,
                        Response<ApiResponse<PagedResponse<Post>>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        PagedResponse<Post> pagedResponse = response.body().getData();
                        displayPosts(pagedResponse != null ? pagedResponse.getContent() : null);
                    } else {
                        showEmpty();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<PagedResponse<Post>>> call, Throwable t) {
                    handleError(t);
                }
            });
        }
    }

    private void displayPosts(List<Post> posts) {
        if (posts != null && !posts.isEmpty()) {
            List<PostItem> postItems = new ArrayList<>();
            for (Post post : posts) {
                PostItem item = new PostItem(
                        post.getId(),
                        post.getSellerName(),
                        formatTime(post.getCreatedAt()),
                        post.getDescription() != null ? post.getDescription() : post.getTitle(),
                        formatPrice(post.getPrice(), post.getUnit()),
                        post.getLikeCount(),
                        post.getCommentCount(),
                        post.getViewCount(),
                        post.isSellerVerified(),
                        post.getStatus());
                item.isLiked = post.isLiked();
                if (post.getImages() != null && !post.getImages().isEmpty()) {
                    item.imageUrl = post.getImages().get(0);
                    item.imageCount = post.getImages().size();
                } else {
                    item.imageCount = 0;
                }
                postItems.add(item);
            }

            postAdapter.setData(postItems);
            rvPosts.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        } else {
            showEmpty();
        }
    }

    private void handleError(Throwable t) {
        progressBar.setVisibility(View.GONE);
        showEmpty();
        if (getContext() != null) {
            Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvPosts.setVisibility(View.GONE);
    }

    private String formatTime(String createdAt) {
        // Simple format - in real app, use proper date parsing
        if (createdAt == null)
            return "";
        return createdAt.substring(0, Math.min(10, createdAt.length()));
    }

    private String formatPrice(Double price, String unit) {
        if (price == null)
            return null;
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
        public boolean isLiked;
        public String status;
        public int imageCount;

        public PostItem(String id, String userName, String time, String content,
                String price, int likeCount, int commentCount, int viewCount, boolean isVerified, String status) {
            this.id = id;
            this.userName = userName;
            this.time = time;
            this.content = content;
            this.price = price;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.viewCount = viewCount;
            this.isVerified = isVerified;
            this.status = status;
            this.isLiked = false;
        }
    }

    private void toggleLike(String postId, int position) {
        // Optimistic update
        // We need the current state to toggle it.
        // Since we don't have easy access to the item here without modifying adapter, 
        // we can assume the user act on what they see.
        // But better is to let the Adapter helper handle modification or access the item.
        // Let's implement getItem in Adapter or simple logic here.
        
        // Actually best way:
        // 1. Get current item from adapter (need public method)
        // 2. Toggle locally
        // 3. Notify adapter
        // 4. Call API
        
        HomeFragment.PostItem item = postAdapter.getItem(position);
        if (item == null) return;
        
        boolean oldIsLiked = item.isLiked;
        int oldLikeCount = item.likeCount;
        
        // New state
        boolean newIsLiked = !oldIsLiked;
        int newLikeCount = oldLikeCount + (newIsLiked ? 1 : -1);
        
        // Update UI immediately
        postAdapter.updateLikeCount(position, newLikeCount, newIsLiked);
        
        postApi.toggleLike(postId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (!response.isSuccessful()) {
                    // Revert on failure
                    postAdapter.updateLikeCount(position, oldLikeCount, oldIsLiked);
                    Toast.makeText(getContext(), "Không thể thực hiện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // Revert on failure
                postAdapter.updateLikeCount(position, oldLikeCount, oldIsLiked);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
