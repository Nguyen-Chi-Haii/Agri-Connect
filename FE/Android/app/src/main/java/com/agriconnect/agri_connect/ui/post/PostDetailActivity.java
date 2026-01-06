package com.agriconnect.agri_connect.ui.post;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private ImageView btnBack;
    private CircleImageView ivAvatar;
    private TextView tvUserName, tvPostTime, tvTitle, tvContent, tvPrice;
    private TextView tvQuantity, tvLocation, tvLikeCount, tvViews, tvNoComments;
    private ImageView ivVerified, ivLike;
    private View btnLike;
    private RecyclerView rvComments;
    private TextInputEditText etComment;
    private ImageView btnSendComment;
    private ProgressBar progressBar;

    private PostApi postApi;
    private String postId;
    private Post currentPost;
    private boolean isLiked = false;
    private int likeCount = 0;

    private CommentAdapter commentAdapter;
    private List<Comment> comments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null) {
            finish();
            return;
        }

        postApi = ApiClient.getInstance(this).getPostApi();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadPostDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvPostTime = findViewById(R.id.tvPostTime);
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvLocation = findViewById(R.id.tvLocation);
        tvLikeCount = findViewById(R.id.tvLikeCount);
        tvViews = findViewById(R.id.tvViews);
        tvNoComments = findViewById(R.id.tvNoComments);
        ivVerified = findViewById(R.id.ivVerified);
        ivLike = findViewById(R.id.ivLike);
        btnLike = findViewById(R.id.btnLike);
        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(comments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnLike.setOnClickListener(v -> toggleLike());

        btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void loadPostDetail() {
        progressBar.setVisibility(View.VISIBLE);

        postApi.getPostById(postId).enqueue(new Callback<ApiResponse<Post>>() {
            @Override
            public void onResponse(Call<ApiResponse<Post>> call, Response<ApiResponse<Post>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentPost = response.body().getData();
                    displayPost(currentPost);
                } else {
                    Toast.makeText(PostDetailActivity.this, "Không thể tải bài đăng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Post>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PostDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayPost(Post post) {
        tvUserName.setText(post.getSellerName() != null ? post.getSellerName() : "Người dùng");
        tvPostTime.setText(formatTime(post.getCreatedAt()));
        tvTitle.setText(post.getTitle());
        tvContent.setText(post.getDescription());

        if (post.getPrice() != null) {
            String priceText = String.format("%,.0f", post.getPrice()) + "đ";
            if (post.getUnit() != null) {
                priceText += "/" + post.getUnit();
            }
            tvPrice.setText(priceText);
            tvPrice.setVisibility(View.VISIBLE);
        } else {
            tvPrice.setVisibility(View.GONE);
        }

        if (post.getQuantity() != null && post.getQuantity() > 0) {
            tvQuantity.setText(String.format("%,.0f %s", post.getQuantity(), 
                    post.getUnit() != null ? post.getUnit() : ""));
        } else {
            tvQuantity.setText("Liên hệ");
        }

        // Use getLocation() from Post model (which is an object now)
        String location = post.getLocation() != null ? post.getLocation().toString() : "Không xác định";
        tvLocation.setText(location);
        tvViews.setText(post.getViewCount() + " lượt xem");

        if (post.isSellerVerified()) {
            ivVerified.setVisibility(View.VISIBLE);
        }

        // Like count (placeholder - should come from API)
        likeCount = 0;
        updateLikeUI();

        // Load demo comments
        loadDemoComments();
    }

    private void toggleLike() {
        isLiked = !isLiked;
        if (isLiked) {
            likeCount++;
        } else {
            likeCount--;
        }
        updateLikeUI();

        // TODO: Call API to like/unlike
    }

    private void updateLikeUI() {
        ivLike.setColorFilter(ContextCompat.getColor(this, 
                isLiked ? R.color.error : R.color.text_secondary));
        tvLikeCount.setText(likeCount + " thích");
        tvLikeCount.setTextColor(ContextCompat.getColor(this, 
                isLiked ? R.color.error : R.color.text_secondary));
    }

    private void sendComment() {
        String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";
        if (comment.isEmpty()) {
            return;
        }

        // Add comment locally (demo)
        Comment newComment = new Comment("current_user", "Bạn", comment, "Vừa xong");
        comments.add(0, newComment);
        commentAdapter.notifyItemInserted(0);
        rvComments.scrollToPosition(0);
        etComment.setText("");

        tvNoComments.setVisibility(View.GONE);
        rvComments.setVisibility(View.VISIBLE);

        // TODO: Call API to add comment
        Toast.makeText(this, "Đã thêm bình luận", Toast.LENGTH_SHORT).show();
    }

    private void loadDemoComments() {
        comments.clear();
        comments.add(new Comment("user1", "Trần Văn B", "Sản phẩm rất tốt!", "2 giờ trước"));
        comments.add(new Comment("user2", "Lê Thị C", "Giá có thể giảm không ạ?", "3 giờ trước"));
        commentAdapter.notifyDataSetChanged();

        if (comments.isEmpty()) {
            tvNoComments.setVisibility(View.VISIBLE);
            rvComments.setVisibility(View.GONE);
        } else {
            tvNoComments.setVisibility(View.GONE);
            rvComments.setVisibility(View.VISIBLE);
        }
    }

    private String formatTime(String createdAt) {
        if (createdAt == null) return "";
        return createdAt.substring(0, Math.min(10, createdAt.length()));
    }

    // Comment data class
    public static class Comment {
        public String id;
        public String userName;
        public String content;
        public String time;

        public Comment(String id, String userName, String content, String time) {
            this.id = id;
            this.userName = userName;
            this.content = content;
            this.time = time;
        }
    }
}
