package com.agriconnect.agri_connect.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.AdminApi;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.model.ApiResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalUsers, tvTotalPosts, tvPendingPosts, tvTotalMessages;
    private CardView cardPostManagement, cardUserManagement;
    private ProgressBar progressBar;
    private AdminApi adminApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupListeners();
        loadDashboardStats();
    }

    private void initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalPosts = findViewById(R.id.tvTotalPosts);
        tvPendingPosts = findViewById(R.id.tvPendingPosts);
        tvTotalMessages = findViewById(R.id.tvTotalMessages);
        cardPostManagement = findViewById(R.id.cardPostManagement);
        cardUserManagement = findViewById(R.id.cardUserManagement);
        progressBar = findViewById(R.id.progressBar);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        adminApi = ApiClient.getInstance(this).getAdminApi();
    }

    private void setupListeners() {
        cardPostManagement.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminPostsActivity.class));
        });

        cardUserManagement.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUsersActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        progressBar.setVisibility(View.VISIBLE);

        adminApi.getDashboardStats().enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                    Response<ApiResponse<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> stats = response.body().getData();

                    tvTotalUsers.setText(formatNumber(stats.get("totalUsers")));
                    tvTotalPosts.setText(formatNumber(stats.get("totalPosts")));
                    tvPendingPosts.setText(formatNumber(stats.get("pendingPosts")));
                    tvTotalMessages.setText(formatNumber(stats.get("totalMessages")));
                } else {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Không thể tải thống kê", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatNumber(Object value) {
        if (value == null)
            return "0";
        if (value instanceof Double) {
            return String.valueOf(((Double) value).intValue());
        }
        return String.valueOf(value);
    }
}
