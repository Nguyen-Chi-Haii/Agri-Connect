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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.AdminApi;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalPosts, tvPendingPosts, tvTotalMessages, tvAdminName;
    private TextView tvGreeting, tvDate, tvUrgentCount;
    private CardView cardUrgent, btnQuickApprove;
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
        setupHooks();
        updateDateTimeGreeting();
        loadDashboardStats();
    }

    private void initViews(View view) {
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalPosts = view.findViewById(R.id.tvTotalPosts);
        tvPendingPosts = view.findViewById(R.id.tvPendingPosts);
        tvTotalMessages = view.findViewById(R.id.tvTotalMessages);
        tvAdminName = view.findViewById(R.id.tvAdminName);
        
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvDate = view.findViewById(R.id.tvDate);
        tvUrgentCount = view.findViewById(R.id.tvUrgentCount);
        cardUrgent = view.findViewById(R.id.cardUrgent);
        btnQuickApprove = view.findViewById(R.id.btnQuickApprove);
        
        progressBar = view.findViewById(R.id.progressBar);

        if (getContext() != null) {
            adminApi = ApiClient.getInstance(getContext()).getAdminApi();
            String adminName = ApiClient.getInstance(getContext()).getTokenManager().getUserName();
            if (adminName != null && tvAdminName != null) {
                tvAdminName.setText(adminName);
            }
        }
    }

    private void setupHooks() {
        if (btnQuickApprove != null) {
            btnQuickApprove.setOnClickListener(v -> {
                // Navigate to Posts tab
                if (getActivity() instanceof AdminMainActivity) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_admin_posts);
                    }
                }
            });
        }
        
        if (cardUrgent != null) {
            cardUrgent.setOnClickListener(v -> {
                // Also navigate to Posts tab
                if (getActivity() instanceof AdminMainActivity) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_admin_posts);
                    }
                }
            });
        }


        if (getView() != null) {
            View cardCategoryManagement = getView().findViewById(R.id.cardCategoryManagement);
            if (cardCategoryManagement != null) {
                cardCategoryManagement.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(getContext(), CategoryManagementActivity.class);
                    startActivity(intent);
                });
            }
        }
    }

    private void updateDateTimeGreeting() {
        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        if (tvDate != null) {
            tvDate.setText(sdf.format(new Date()));
        }

        // Greeting
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting = "Xin chào";
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Chào buổi sáng";
        } else if (timeOfDay >= 12 && timeOfDay < 18) {
            greeting = "Chào buổi chiều";
        } else if (timeOfDay >= 18 && timeOfDay < 24) {
            greeting = "Chào buổi tối";
        }
        
        if (tvGreeting != null) {
            tvGreeting.setText(greeting + ",");
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
                        
                        // Urgent Widget
                        checkUrgentActions(stats.get("pendingPosts"));
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

    private void checkUrgentActions(Object pendingPostsObj) {
        int pendingCount = 0;
        try {
            if (pendingPostsObj instanceof Number) {
                pendingCount = ((Number) pendingPostsObj).intValue();
            } else if (pendingPostsObj instanceof String) {
                pendingCount = Integer.parseInt((String) pendingPostsObj);
            }
        } catch (Exception e) {
            pendingCount = 0;
        }

        if (cardUrgent != null && tvUrgentCount != null) {
            if (pendingCount > 0) {
                cardUrgent.setVisibility(View.VISIBLE);
                tvUrgentCount.setText("Có " + pendingCount + " bài đăng đang chờ duyệt");
            } else {
                cardUrgent.setVisibility(View.GONE);
            }
        }
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
