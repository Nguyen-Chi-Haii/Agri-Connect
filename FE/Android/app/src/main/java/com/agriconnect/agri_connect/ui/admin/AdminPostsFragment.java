package com.agriconnect.agri_connect.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class AdminPostsFragment extends Fragment implements AdminPostAdapter.OnPostActionListener {

    private RecyclerView recyclerPosts;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private Chip chipAll, chipPending, chipApproved, chipRejected, chipClosed;
    private com.agriconnect.agri_connect.ui.component.PaginationControl paginationControl;

    private AdminPostAdapter adapter;
    private AdminApi adminApi;
    // Removed allPosts list as we fetch per page
    private String currentFilter = null; // null = All
    private int PAGE_SIZE = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupFilters();
        setupPagination();
        loadPosts(currentFilter, 0);
    }

    private void initViews(View view) {
        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipApproved = view.findViewById(R.id.chipApproved);
        chipRejected = view.findViewById(R.id.chipRejected);
        chipClosed = view.findViewById(R.id.chipClosed);
        paginationControl = view.findViewById(R.id.paginationControl);

        if (getContext() != null) {
            adminApi = ApiClient.getInstance(getContext()).getAdminApi();
        }
    }

    private void setupRecyclerView() {
        adapter = new AdminPostAdapter();
        adapter.setOnPostActionListener(this);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPosts.setAdapter(adapter);
    }

    private void setupFilters() {
        View.OnClickListener filterListener = v -> {
            chipAll.setChecked(v == chipAll);
            chipPending.setChecked(v == chipPending);
            chipApproved.setChecked(v == chipApproved);
            chipRejected.setChecked(v == chipRejected);
            chipClosed.setChecked(v == chipClosed);

            if (v == chipAll)
                currentFilter = null;
            else if (v == chipPending)
                currentFilter = "PENDING";
            else if (v == chipApproved)
                currentFilter = "APPROVED";
            else if (v == chipRejected)
                currentFilter = "REJECTED";
            else if (v == chipClosed)
                currentFilter = "CLOSED";

            // Reload from page 0 when filter changes
            loadPosts(currentFilter, 0);
        };

        chipAll.setOnClickListener(filterListener);
        chipPending.setOnClickListener(filterListener);
        chipApproved.setOnClickListener(filterListener);
        chipRejected.setOnClickListener(filterListener);
        chipClosed.setOnClickListener(filterListener);
    }

    private void setupPagination() {
        paginationControl.setOnPageChangeListener(newPage -> {
            loadPosts(currentFilter, newPage);
        });
    }

    private void loadPosts(String status, int page) {
        progressBar.setVisibility(View.VISIBLE);

        // Call API with status, page, size
        adminApi.getAllPosts(status, page, PAGE_SIZE).enqueue(new Callback<ApiResponse<PagedResponse<Post>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<Post>>> call,
                    Response<ApiResponse<PagedResponse<Post>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PagedResponse<Post> pagedResponse = response.body().getData();
                    List<Post> posts = pagedResponse != null && pagedResponse.getContent() != null
                            ? pagedResponse.getContent()
                            : new ArrayList<>();

                    adapter.setPosts(posts);
                    
                    // Update UI state
                    tvEmpty.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerPosts.setVisibility(posts.isEmpty() ? View.GONE : View.VISIBLE);
                    
                    // Update pagination control
                    if (pagedResponse != null) {
                        paginationControl.setPageData(pagedResponse.getCurrentPage(), pagedResponse.getTotalPages());
                        paginationControl.setVisibility(posts.isEmpty() ? View.GONE : View.VISIBLE); 
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<Post>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(Post post) {
        new AlertDialog.Builder(getContext())
                .setTitle("Duyệt bài đăng")
                .setMessage("Bạn có chắc muốn duyệt bài đăng này?")
                .setPositiveButton("Duyệt", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    adminApi.approvePost(post.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã duyệt", Toast.LENGTH_SHORT).show();
                                loadPosts(currentFilter, 0); // Reload current view
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onReject(Post post) {
        EditText input = new EditText(getContext());
        input.setHint("Lý do từ chối");

        new AlertDialog.Builder(getContext())
                .setTitle("Từ chối bài đăng")
                .setView(input)
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    String reason = input.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);

                    adminApi.rejectPost(post.getId(), reason).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã từ chối", Toast.LENGTH_SHORT).show();
                                loadPosts(currentFilter, 0); // Reload current view
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onClose(Post post) {
        new AlertDialog.Builder(getContext())
                .setTitle("Đóng bài đăng")
                .setMessage("Bạn có chắc chắn muốn đóng bài đăng này? Người dùng sẽ không thể tìm thấy bài đăng này nữa.")
                .setPositiveButton("Đóng", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    adminApi.closePost(post.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã đóng bài đăng", Toast.LENGTH_SHORT).show();
                                loadPosts(currentFilter, 0); // Reload current view
                            } else {
                                Toast.makeText(getContext(), "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
