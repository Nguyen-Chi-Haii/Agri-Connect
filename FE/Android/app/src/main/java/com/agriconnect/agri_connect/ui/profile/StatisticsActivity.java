package com.agriconnect.agri_connect.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.UserApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.StatisticsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalPosts, tvApprovedPosts, tvPendingPosts, tvInteractions;
    private ProgressBar progressBar;
    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userApi = ApiClient.getInstance(this).getUserApi();

        initViews();
        setupListeners();
        loadStatistics();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalPosts = findViewById(R.id.tvTotalPosts);
        tvApprovedPosts = findViewById(R.id.tvApprovedPosts);
        tvPendingPosts = findViewById(R.id.tvPendingPosts);
        tvInteractions = findViewById(R.id.tvInteractions);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        userApi.getStatistics().enqueue(new Callback<ApiResponse<StatisticsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StatisticsResponse>> call,
                    Response<ApiResponse<StatisticsResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    displayStatistics(response.body().getData());
                } else {
                    Toast.makeText(StatisticsActivity.this, "Không thể tải thống kê", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StatisticsResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StatisticsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStatistics(StatisticsResponse stats) {
        if (stats == null)
            return;
        tvTotalPosts.setText(String.valueOf(stats.getTotalPosts()));
        tvApprovedPosts.setText(String.valueOf(stats.getApprovedPosts()));
        tvPendingPosts.setText(String.valueOf(stats.getPendingPosts()));
        tvInteractions.setText(String.valueOf(stats.getTotalInteractions()));
    }
}
