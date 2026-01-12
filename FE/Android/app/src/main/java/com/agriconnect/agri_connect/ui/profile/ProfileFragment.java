package com.agriconnect.agri_connect.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.TokenManager;
import com.agriconnect.agri_connect.api.UserApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.UserProfile;
import com.agriconnect.agri_connect.ui.admin.AdminDashboardActivity;
import com.agriconnect.agri_connect.ui.auth.RoleSelectionActivity;
import com.agriconnect.agri_connect.ui.post.MyPostsActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView ivAvatar, ivVerified;
    private TextView tvUserName, tvRole, tvPhone;
    private LinearLayout btnMyPosts, btnEditProfile, btnSettings, btnAdmin;
    private MaterialButton btnLogout;

    private TokenManager tokenManager;
    private UserApi userApi;

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    loadProfile();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API
        if (getContext() != null) {
            ApiClient apiClient = ApiClient.getInstance(getContext());
            tokenManager = apiClient.getTokenManager();
            userApi = apiClient.getUserApi();
        }

        initViews(view);
        loadProfile();
        setupListeners();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivVerified = view.findViewById(R.id.ivVerified);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvRole = view.findViewById(R.id.tvRole);
        tvPhone = view.findViewById(R.id.tvPhone);
        btnMyPosts = view.findViewById(R.id.btnMyPosts);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnAdmin = view.findViewById(R.id.btnAdmin);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadProfile() {
        // First show cached data
        String cachedName = tokenManager.getUserName();
        String cachedRole = tokenManager.getUserRole();

        if (cachedName != null) {
            tvUserName.setText(cachedName);
        }
        if (cachedRole != null) {
            tvRole.setText("FARMER".equals(cachedRole) ? R.string.role_farmer : R.string.role_trader);
            // Show admin button if cached role is ADMIN
            if (btnAdmin != null) {
                btnAdmin.setVisibility("ADMIN".equals(cachedRole) ? View.VISIBLE : View.GONE);
            }
        }

        // Then fetch from API
        userApi.getProfile().enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserProfile profile = response.body().getData();
                    if (profile != null) {
                        updateUI(profile);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                // Keep showing cached data
            }
        });
    }

    private void updateUI(UserProfile profile) {
        tvUserName.setText(profile.getFullName());
        tvPhone.setText(profile.getPhone());

        String role = profile.getRole();
        android.util.Log.d("ProfileFragment", "User role from API: " + role);

        if ("FARMER".equals(role)) {
            tvRole.setText(R.string.role_farmer);
        } else if ("TRADER".equals(role)) {
            tvRole.setText(R.string.role_trader);
        } else if ("ADMIN".equals(role)) {
            tvRole.setText("Quản trị viên");
        } else {
            tvRole.setText(role);
        }

        ivVerified.setVisibility(profile.isVerified() ? View.VISIBLE : View.GONE);

        // Show admin button only for ADMIN role
        boolean isAdmin = "ADMIN".equals(role);
        android.util.Log.d("ProfileFragment", "Is admin: " + isAdmin);

        if (btnAdmin != null) {
            btnAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        View dividerAdmin = getView() != null ? getView().findViewById(R.id.dividerAdmin) : null;
        if (dividerAdmin != null) {
            dividerAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    private void setupListeners() {
        btnMyPosts.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyPostsActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StatisticsActivity.class);
            startActivity(intent);
        });

        // Admin button
        if (btnAdmin != null) {
            btnAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AdminDashboardActivity.class);
                startActivity(intent);
            });
        }

        btnLogout.setOnClickListener(v -> {
            // Clear tokens
            tokenManager.clearTokens();
            ApiClient.resetInstance();

            // Navigate to login
            Intent intent = new Intent(getActivity(), RoleSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}
