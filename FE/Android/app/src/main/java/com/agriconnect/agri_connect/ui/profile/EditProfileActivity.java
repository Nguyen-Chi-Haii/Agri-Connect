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
import com.bumptech.glide.Glide;

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

    private View layoutKycInfo, layoutTaxCodeInfo, layoutCccdInfo;
    private TextView tvTaxCodeInfo, tvCccdInfo;
    private ImageView ivCccdFront, ivCccdBack;
    
    // New editable tax code
    private com.google.android.material.textfield.TextInputLayout tilTaxCodeInput;
    private TextInputEditText etTaxCodeInput;

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

        // Editable Tax Code
        tilTaxCodeInput = findViewById(R.id.tilTaxCodeInput);
        etTaxCodeInput = findViewById(R.id.etTaxCodeInput);

        // KYC Info Views (Read-only section)
        layoutKycInfo = findViewById(R.id.layoutKycInfo);
        layoutTaxCodeInfo = findViewById(R.id.layoutTaxCodeInfo);
        layoutCccdInfo = findViewById(R.id.layoutCccdInfo);
        tvTaxCodeInfo = findViewById(R.id.tvTaxCodeInfo);
        tvCccdInfo = findViewById(R.id.tvCccdInfo);
        ivCccdFront = findViewById(R.id.ivCccdFront);
        ivCccdBack = findViewById(R.id.ivCccdBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> updateProfile());

        btnEkyc.setOnClickListener(v -> {
            if (currentProfile != null && "TRADER".equals(currentProfile.getRole())) {
                 // For Trader, just focus the tax code field
                 etTaxCodeInput.requestFocus();
                 Toast.makeText(this, "Vui lòng nhập Mã số thuế ở trên và bấm Lưu", Toast.LENGTH_SHORT).show();
                 return;
            }

            if (currentProfile != null && currentProfile.getKyc() != null) {
                String status = currentProfile.getKyc().getStatus();
                if ("VERIFIED".equals(status)) {
                    Toast.makeText(this, "Tài khoản của bạn đã được xác minh", Toast.LENGTH_SHORT).show();
                    return;
                } else if ("PENDING".equals(status)) {
                    Toast.makeText(this, "Hồ sơ của bạn đang chờ duyệt, vui lòng đợi", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Allow if NULL or REJECTED (Farmer only)
            Intent intent = new Intent(this, EkycUploadActivity.class);
            startActivity(intent);
        });

        btnChangeAvatar.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đổi ảnh đại diện đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayProfile(UserProfile profile) {
        etFullName.setText(profile.getFullName());
        etPhone.setText(profile.getPhone());
        etAddress.setText(profile.getAddress());
        
        // Show/Hide Tax Code Input based on Role
        if ("TRADER".equals(profile.getRole())) {
            tilTaxCodeInput.setVisibility(View.VISIBLE);
            if (profile.getKyc() != null && profile.getKyc().getTaxCode() != null) {
                etTaxCodeInput.setText(profile.getKyc().getTaxCode());
            }
        } else {
            tilTaxCodeInput.setVisibility(View.GONE);
        }

        if (profile.getKyc() != null) {
            com.agriconnect.agri_connect.api.model.KycInfo kyc = profile.getKyc();
            String status = kyc.getStatus();
            
            // 1. Status Text
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

            // 2. Info Display
            layoutKycInfo.setVisibility(View.VISIBLE);
            
            // Check Role OR check which data is available
            if ("TRADER".equals(profile.getRole())) {
                // For Trader, we already have editable input, but let's keep read-only info hidden or visible?
                // Maybe hide read-only for trader since we have input?
                // Let's hide read-only section for Trader to avoid duplication, or show it?
                // User requirement: "show sent info". 
                // Let's show it if verified/pending, but maybe hide if we are editing?
                // Simpler: Show it as is.
                layoutTaxCodeInfo.setVisibility(View.VISIBLE);
                layoutCccdInfo.setVisibility(View.GONE);
                tvTaxCodeInfo.setText(kyc.getTaxCode() != null ? kyc.getTaxCode() : "Chưa cập nhật");
            } else if ("FARMER".equals(profile.getRole())) {
                layoutTaxCodeInfo.setVisibility(View.GONE);
                layoutCccdInfo.setVisibility(View.VISIBLE);
                tvCccdInfo.setText(kyc.getCccd() != null ? kyc.getCccd() : "Chưa cập nhật");

                // Load Images using Glide
                if (kyc.getCccdFrontImage() != null) {
                    Glide.with(this)
                        .load(kyc.getCccdFrontImage())
                        .placeholder(R.drawable.ic_gallery)
                        .into(ivCccdFront);
                } else {
                    ivCccdFront.setImageResource(R.drawable.ic_gallery);
                }

                if (kyc.getCccdBackImage() != null) {
                     Glide.with(this)
                        .load(kyc.getCccdBackImage())
                         .placeholder(R.drawable.ic_gallery)
                        .into(ivCccdBack);
                } else {
                    ivCccdBack.setImageResource(R.drawable.ic_gallery);
                }
            } else {
                layoutKycInfo.setVisibility(View.GONE); // Admin or others
            }
        } else {
             layoutKycInfo.setVisibility(View.GONE);
        }
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String taxCode = etTaxCodeInput.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            return;
        }
        
        // Validate Tax Code for Trader if visible
        if (tilTaxCodeInput.getVisibility() == View.VISIBLE && taxCode.isEmpty()) {
            etTaxCodeInput.setError("Vui lòng nhập Mã số thuế");
            return;
        }

        showLoading(true);
        UserProfile updateReq = new UserProfile();
        updateReq.setFullName(fullName);
        updateReq.setPhone(phone);
        updateReq.setAddress(address);
        
        if (tilTaxCodeInput.getVisibility() == View.VISIBLE) {
            updateReq.setTaxCode(taxCode);
        }

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

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}
