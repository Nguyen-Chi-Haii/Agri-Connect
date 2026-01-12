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
import com.agriconnect.agri_connect.api.model.UserProfile;
import com.agriconnect.agri_connect.ui.admin.adapter.AdminUserAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersActivity extends AppCompatActivity implements AdminUserAdapter.OnUserActionListener {

    private RecyclerView recyclerUsers;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private AdminUserAdapter adapter;
    private AdminApi adminApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        initViews();
        setupRecyclerView();
        loadUsers();
    }

    private void initViews() {
        recyclerUsers = findViewById(R.id.recyclerUsers);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        adminApi = ApiClient.getInstance(this).getAdminApi();
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter();
        adapter.setOnUserActionListener(this);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(adapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        adminApi.getAllUsers().enqueue(new Callback<ApiResponse<List<UserProfile>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserProfile>>> call,
                    Response<ApiResponse<List<UserProfile>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<UserProfile> users = response.body().getData();
                    adapter.setUsers(users);

                    boolean isEmpty = users == null || users.isEmpty();
                    tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    recyclerUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                } else {
                    Toast.makeText(AdminUsersActivity.this,
                            "Không thể tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserProfile>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminUsersActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVerifyKyc(UserProfile user) {
        new AlertDialog.Builder(this)
                .setTitle("Xác minh KYC")
                .setMessage("Xác nhận xác minh KYC cho người dùng " + user.getFullName() + "?")
                .setPositiveButton("Xác minh", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    adminApi.verifyKyc(user.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call,
                                Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminUsersActivity.this,
                                        "Đã xác minh KYC thành công", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(AdminUsersActivity.this,
                                        "Lỗi khi xác minh KYC", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminUsersActivity.this,
                                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onRejectKyc(UserProfile user) {
        EditText input = new EditText(this);
        input.setHint("Lý do từ chối (tùy chọn)");

        new AlertDialog.Builder(this)
                .setTitle("Từ chối KYC")
                .setView(input)
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    String reason = input.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);

                    adminApi.rejectKyc(user.getId(), reason).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call,
                                Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminUsersActivity.this,
                                        "Đã từ chối KYC", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(AdminUsersActivity.this,
                                        "Lỗi khi từ chối KYC", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminUsersActivity.this,
                                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
