package com.agriconnect.agri_connect.ui.profile;

import android.content.Intent;
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
import com.agriconnect.agri_connect.api.model.UserProfile;
import com.agriconnect.agri_connect.ui.auth.EkycUploadActivity;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnSave, tvEkycStatus;
    private TextInputEditText etFullName, etPhone, etAddress;
    private ImageView ivAvatar;
    private View btnChangeAvatar, btnEkyc;
    private ProgressBar progressBar;

    private UserApi userApi;
    private UserProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userApi = ApiClient.getInstance(this).getUserApi();

        initViews();
        setupListeners();
        loadProfile();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        tvEkycStatus = findViewById(R.id.tvEkycStatus);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnEkyc = findViewById(R.id.btnEkyc);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> updateProfile());

        btnEkyc.setOnClickListener(v -> {
            Intent intent = new Intent(this, EkycUploadActivity.class);
            startActivity(intent);
        });

        btnChangeAvatar.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đổi ảnh đại diện đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProfile() {
        showLoading(true);
        userApi.getProfile().enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentProfile = response.body().getData();
                    displayProfile(currentProfile);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Lỗi tải thông tin: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void displayProfile(UserProfile profile) {
        etFullName.setText(profile.getFullName());
        etPhone.setText(profile.getPhone());
        etAddress.setText(profile.getAddress());

        if (profile.getKyc() != null) {
            String status = profile.getKyc().getStatus();
            if ("VERIFIED".equals(status)) {
                tvEkycStatus.setText("Đã xác thực");
                tvEkycStatus.setTextColor(getResources().getColor(R.color.farmer_green));
            } else if ("PENDING".equals(status)) {
                tvEkycStatus.setText("Đang chờ duyệt");
                tvEkycStatus.setTextColor(getResources().getColor(R.color.warning));
            } else if ("REJECTED".equals(status)) {
                tvEkycStatus.setText("Bị từ chối");
                tvEkycStatus.setTextColor(getResources().getColor(R.color.error));
            }
        }
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            return;
        }

        showLoading(true);
        UserProfile updateReq = new UserProfile();
        updateReq.setFullName(fullName);
        updateReq.setPhone(phone);
        updateReq.setAddress(address);

        userApi.updateProfile(updateReq).enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}
