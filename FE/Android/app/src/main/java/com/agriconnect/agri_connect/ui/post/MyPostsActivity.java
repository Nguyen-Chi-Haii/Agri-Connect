package com.agriconnect.agri_connect.ui.post;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.PostApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Post;
import com.agriconnect.agri_connect.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsActivity extends AppCompatActivity implements MyPostsAdapter.OnPostActionListener {

    private ImageView btnBack;
    private RecyclerView rvPosts;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreatePost;
    private View btnCreatePost;

    private PostApi postApi;
    private MyPostsAdapter adapter;
    private List<HomeFragment.PostItem> posts = new ArrayList<>();

    private final ActivityResultLauncher<Intent> createPostLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadMyPosts();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_posts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postApi = ApiClient.getInstance(this).getPostApi();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadMyPosts();
    }

    private com.google.android.material.chip.ChipGroup cgStatus;
    private String currentStatus = null; // null = All

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvPosts = findViewById(R.id.rvPosts);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        fabCreatePost = findViewById(R.id.fabCreatePost);
        btnCreatePost = findViewById(R.id.btnCreatePost);
        cgStatus = findViewById(R.id.cgStatus);
    }

    private void setupRecyclerView() {
        adapter = new MyPostsAdapter(posts, this);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        fabCreatePost.setOnClickListener(v -> openCreatePost());
        
        if (btnCreatePost != null) {
            btnCreatePost.setOnClickListener(v -> openCreatePost());
        }

        cgStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            
            if (id == R.id.chipPending) currentStatus = "PENDING";
            else if (id == R.id.chipApproved) currentStatus = "APPROVED";
            else if (id == R.id.chipRejected) currentStatus = "REJECTED";
            else if (id == R.id.chipClosed) currentStatus = "CLOSED";
            else currentStatus = null; // All

            loadMyPosts(currentStatus);
        });
    }

    private void openCreatePost() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        createPostLauncher.launch(intent);
    }

    private void loadMyPosts() {
        loadMyPosts(currentStatus);
    }

    private void loadMyPosts(String status) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvPosts.setVisibility(View.GONE);

        // API Call with status param
        postApi.getMyPosts(status).enqueue(new Callback<ApiResponse<List<Post>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Post>>> call, Response<ApiResponse<List<Post>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Post> postList = response.body().getData();
                    
                    posts.clear(); // Clear old data

                    if (postList != null && !postList.isEmpty()) {
                        for (Post post : postList) {
                            HomeFragment.PostItem item = new HomeFragment.PostItem(
                                    post.getId(),
                                    post.getSellerName(),
                                    formatTime(post.getCreatedAt()),
                                    post.getDescription() != null ? post.getDescription() : post.getTitle(),
                                    formatPrice(post.getPrice(), post.getUnit()),
                                    0, 0, post.getViewCount(),
                                    post.isSellerVerified(),
                                    post.getStatus()
                            );
                            if (post.getImages() != null && !post.getImages().isEmpty()) {
                                item.imageUrl = post.getImages().get(0);
                                item.imageCount = post.getImages().size();
                            } else {
                                item.imageCount = 0;
                            }
                            posts.add(item);
                        }
                        adapter.notifyDataSetChanged();
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
                Toast.makeText(MyPostsActivity.this, "Lỗi tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvPosts.setVisibility(View.GONE);
    }

    @Override
    public void onClosePost(HomeFragment.PostItem post, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đóng")
                .setMessage("Bạn có chắc muốn đóng bài đăng này? Bài đăng sẽ không còn hiển thị với người mua.")
                .setPositiveButton("Đóng", (dialog, which) -> closePost(post, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void closePost(HomeFragment.PostItem post, int position) {
        progressBar.setVisibility(View.VISIBLE);

        postApi.closePost(post.id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    // Update local status instead of removing
                    post.status = "CLOSED";
                    adapter.notifyItemChanged(position);
                    Toast.makeText(MyPostsActivity.this, "Đã đóng bài đăng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyPostsActivity.this, "Đóng bài đăng thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyPostsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTime(String createdAt) {
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
}
