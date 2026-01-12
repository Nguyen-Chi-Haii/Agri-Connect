package com.agriconnect.agri_connect.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.AdminApi;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.PagedResponse;
import com.agriconnect.agri_connect.api.model.Post;
import com.agriconnect.agri_connect.ui.admin.adapter.AdminPostAdapter;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPostsActivity extends AppCompatActivity implements AdminPostAdapter.OnPostActionListener {

    private RecyclerView recyclerPosts;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private Chip chipAll, chipPending, chipApproved, chipRejected;

    private AdminPostAdapter adapter;
    private AdminApi adminApi;
    private List<Post> allPosts = new ArrayList<>();
    private String currentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_posts);

        initViews();
        setupRecyclerView();
        setupFilters();
        loadPosts();
    }

    private void initViews() {
        recyclerPosts = findViewById(R.id.recyclerPosts);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipApproved = findViewById(R.id.chipApproved);
        chipRejected = findViewById(R.id.chipRejected);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        adminApi = ApiClient.getInstance(this).getAdminApi();
    }

    private void setupRecyclerView() {
        adapter = new AdminPostAdapter();
        adapter.setOnPostActionListener(this);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerPosts.setAdapter(adapter);
    }

    private void setupFilters() {
        View.OnClickListener filterListener = v -> {
            chipAll.setChecked(v == chipAll);
            chipPending.setChecked(v == chipPending);
            chipApproved.setChecked(v == chipApproved);
            chipRejected.setChecked(v == chipRejected);

            if (v == chipAll)
                currentFilter = null;
            else if (v == chipPending)
                currentFilter = "PENDING";
            else if (v == chipApproved)
                currentFilter = "APPROVED";
            else if (v == chipRejected)
                currentFilter = "REJECTED";

            filterPosts();
        };

        chipAll.setOnClickListener(filterListener);
        chipPending.setOnClickListener(filterListener);
        chipApproved.setOnClickListener(filterListener);
        chipRejected.setOnClickListener(filterListener);
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);

        adminApi.getAllPosts().enqueue(new Callback<ApiResponse<PagedResponse<Post>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<Post>>> call,
                    Response<ApiResponse<PagedResponse<Post>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PagedResponse<Post> pagedResponse = response.body().getData();
                    allPosts = pagedResponse != null && pagedResponse.getContent() != null
                            ? pagedResponse.getContent()
                            : new ArrayList<>();
                    filterPosts();
                } else {
                    Toast.makeText(AdminPostsActivity.this,
                            "Không thể tải danh sách bài đăng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<Post>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminPostsActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterPosts() {
        List<Post> filtered;
        if (currentFilter == null) {
            filtered = allPosts;
        } else {
            filtered = new ArrayList<>();
            for (Post post : allPosts) {
                if (currentFilter.equals(post.getStatus())) {
                    filtered.add(post);
                }
            }
        }

        adapter.setPosts(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerPosts.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onApprove(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("Duyệt bài đăng")
                .setMessage("Bạn có chắc muốn duyệt bài đăng này?")
                .setPositiveButton("Duyệt", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    adminApi.approvePost(post.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call,
                                Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminPostsActivity.this,
                                        "Đã duyệt bài đăng", Toast.LENGTH_SHORT).show();
                                loadPosts();
                            } else {
                                Toast.makeText(AdminPostsActivity.this,
                                        "Lỗi khi duyệt bài đăng", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminPostsActivity.this,
                                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onReject(Post post) {
        EditText input = new EditText(this);
        input.setHint("Lý do từ chối (tùy chọn)");

        new AlertDialog.Builder(this)
                .setTitle("Từ chối bài đăng")
                .setView(input)
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    String reason = input.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);

                    adminApi.rejectPost(post.getId(), reason).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call,
                                Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminPostsActivity.this,
                                        "Đã từ chối bài đăng", Toast.LENGTH_SHORT).show();
                                loadPosts();
                            } else {
                                Toast.makeText(AdminPostsActivity.this,
                                        "Lỗi khi từ chối bài đăng", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminPostsActivity.this,
                                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
