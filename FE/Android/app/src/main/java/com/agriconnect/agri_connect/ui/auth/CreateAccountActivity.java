package com.agriconnect.agri_connect.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.AuthApi;
import com.agriconnect.agri_connect.api.TokenManager;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.JwtResponse;
import com.agriconnect.agri_connect.api.model.RegisterRequest;
import com.agriconnect.agri_connect.ui.main.MainNavigationActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAccountActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvRoleBadge;
    private TextInputEditText etFullName, etPhone, etPassword, etConfirmPassword, etAddress;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLoginLink;

    private String selectedRole;
    private AuthApi authApi;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedRole = getIntent().getStringExtra("role");
        if (selectedRole == null) {
            selectedRole = "FARMER";
        }

        // Initialize API
        ApiClient apiClient = ApiClient.getInstance(this);
        authApi = apiClient.getAuthApi();
        tokenManager = apiClient.getTokenManager();

        initViews();
        setupViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupViews() {
        // Set role badge
        if ("FARMER".equals(selectedRole)) {
            tvRoleBadge.setText(R.string.role_farmer);
            tvRoleBadge.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.farmer_green));
        } else {
            tvRoleBadge.setText(R.string.role_trader);
            tvRoleBadge.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.trader_blue));
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> attemptRegister());

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (!phone.matches("^(0|\\+84)[0-9]{9,10}$")) {
            etPhone.setError("Số điện thoại không hợp lệ");
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        showLoading(true);

        // Call register API
        RegisterRequest request = new RegisterRequest(phone, password, fullName, address, selectedRole);
        authApi.register(request).enqueue(new Callback<ApiResponse<JwtResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<JwtResponse>> call, Response<ApiResponse<JwtResponse>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    JwtResponse jwt = response.body().getData();
                    
                    // Save tokens
                    tokenManager.saveTokens(jwt.getAccessToken(), jwt.getRefreshToken());
                    tokenManager.saveUserInfo(jwt.getUserId(), jwt.getFullName(), jwt.getRole());
                    
                    Toast.makeText(CreateAccountActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to eKYC upload (optional)
                    Intent intent = new Intent(CreateAccountActivity.this, EkycUploadActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Đăng ký thất bại";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(CreateAccountActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<JwtResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CreateAccountActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        btnRegister.setText(show ? "" : getString(R.string.register));
    }
}
