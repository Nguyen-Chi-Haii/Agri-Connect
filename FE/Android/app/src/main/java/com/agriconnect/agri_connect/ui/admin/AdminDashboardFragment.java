package com.agriconnect.agri_connect.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.AdminApi;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.model.ApiResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalPosts, tvPendingPosts, tvTotalMessages, tvAdminName;
    private ProgressBar progressBar;
    private AdminApi adminApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadDashboardStats();
    }

    private void initViews(View view) {
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalPosts = view.findViewById(R.id.tvTotalPosts);
        tvPendingPosts = view.findViewById(R.id.tvPendingPosts);
        tvTotalMessages = view.findViewById(R.id.tvTotalMessages);
        tvAdminName = view.findViewById(R.id.tvAdminName);
        progressBar = view.findViewById(R.id.progressBar);

        if (getContext() != null) {
            adminApi = ApiClient.getInstance(getContext()).getAdminApi();
            String adminName = ApiClient.getInstance(getContext()).getTokenManager().getUserName();
            if (adminName != null && tvAdminName != null) {
                tvAdminName.setText(adminName);
            }
        }
    }

    private void loadDashboardStats() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);

        adminApi.getDashboardStats().enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                    Response<ApiResponse<Map<String, Object>>> response) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> stats = response.body().getData();
                    if (stats != null) {
                        tvTotalUsers.setText(formatNumber(stats.get("totalUsers")));
                        tvTotalPosts.setText(formatNumber(stats.get("totalPosts")));
                        tvPendingPosts.setText(formatNumber(stats.get("pendingPosts")));
                        tvTotalMessages.setText(formatNumber(stats.get("totalMessages")));
                    }
                } else {
                    showError("Không thể tải thống kê");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                showError("Lỗi kết nối: " + t.getMessage());
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

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
