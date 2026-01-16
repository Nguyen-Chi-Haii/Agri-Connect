package com.agriconnect.agri_connect.ui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAccountActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvRoleBadge;
    private TextInputEditText etFullName, etUsername, etPhone, etPassword, etConfirmPassword, etAddress, etTaxCode;
    private com.google.android.material.textfield.TextInputLayout tilTaxCode, tilAddress;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLoginLink;

    private String selectedRole;
    private AuthApi authApi;
    private TokenManager tokenManager;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Cần quyền truy cập vị trí để lấy địa chỉ", Toast.LENGTH_SHORT).show();
                }
            });

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

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAddress = findViewById(R.id.etAddress);
        etTaxCode = findViewById(R.id.etTaxCode);
        tilTaxCode = findViewById(R.id.tilTaxCode);
        tilAddress = findViewById(R.id.tilAddress);
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
            tilTaxCode.setVisibility(View.VISIBLE);
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

        // GPS Location button
        tilAddress.setEndIconOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    private void attemptRegister() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim()
                : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (username.length() < 3) {
            etUsername.setError("Tên đăng nhập phải có ít nhất 3 ký tự");
            etUsername.requestFocus();
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

        String taxCode = etTaxCode.getText() != null ? etTaxCode.getText().toString().trim() : "";

        // For TRADER, taxCode is optional now. For FARMER, explicitly null.
        if (!"TRADER".equals(selectedRole)) {
            taxCode = null;
        }

        showLoading(true);

        // Call register API
        RegisterRequest request = new RegisterRequest(username, phone, password, fullName, address, selectedRole,
                taxCode);
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

                    // Navigate based on role
                    Intent intent;
                    if ("TRADER".equals(jwt.getRole())) {
                        // Trader skips eKYC upload (relies on Tax Code)
                        intent = new Intent(CreateAccountActivity.this, MainNavigationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    } else {
                        // Farmer proceeds to eKYC upload
                        intent = new Intent(CreateAccountActivity.this, EkycUploadActivity.class);
                    }
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

    private boolean isLocationInVietnam(double latitude, double longitude) {
        // Broad bounding box for Vietnam
        return latitude >= 8.0 && latitude <= 24.0 &&
                longitude >= 102.0 && longitude <= 110.0;
    }

    private void getCurrentLocation() {
        tilAddress.setEndIconActivated(false);

        // Default location (Ho Chi Minh City) for fallback
        double defaultLat = 10.762622;
        double defaultLng = 106.660172;

        // Check permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Dùng vị trí mặc định (TP.HCM)", Toast.LENGTH_SHORT).show();
            getAddressFromLocation(defaultLat, defaultLng);
            return;
        }

        Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();

        // Try GPS with quick timeout
        android.os.Handler handler = new android.os.Handler(getMainLooper());
        final boolean[] locationReceived = { false };

        // Timeout after 3 seconds
        handler.postDelayed(() -> {
            if (!locationReceived[0]) {
                locationReceived[0] = true;
                tilAddress.setEndIconActivated(true);
                Toast.makeText(this, "Không nhận được tín hiệu GPS, dùng vị trí mặc định (TP.HCM)", Toast.LENGTH_SHORT)
                        .show();
                getAddressFromLocation(defaultLat, defaultLng);
            }
        }, 3000);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (!locationReceived[0]) {
                        locationReceived[0] = true;
                        tilAddress.setEndIconActivated(true);

                        if (location != null) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();

                            if (isLocationInVietnam(lat, lng)) {
                                getAddressFromLocation(lat, lng);
                            } else {
                                // Location is outside Vietnam (likely emulator default)
                                Toast.makeText(this, "Vị trí GPS không thuộc Việt Nam, dùng vị trí mặc định (TP.HCM)",
                                        Toast.LENGTH_LONG).show();
                                getAddressFromLocation(defaultLat, defaultLng);
                            }
                        } else {
                            Toast.makeText(this, "Không lấy được vị trí, dùng vị trí mặc định (TP.HCM)",
                                    Toast.LENGTH_SHORT).show();
                            getAddressFromLocation(defaultLat, defaultLng);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!locationReceived[0]) {
                        locationReceived[0] = true;
                        tilAddress.setEndIconActivated(true);
                        Toast.makeText(this, "Lỗi GPS, dùng vị trí mặc định (TP.HCM)", Toast.LENGTH_SHORT).show();
                        getAddressFromLocation(defaultLat, defaultLng);
                    }
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        // Use Nominatim (OpenStreetMap) API
        new Thread(() -> {
            try {
                String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                        + latitude + "&lon=" + longitude + "&accept-language=vi";

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .header("User-Agent", "AgriConnect/1.0")
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    org.json.JSONObject jsonObject = new org.json.JSONObject(json);
                    String displayName = jsonObject.optString("display_name", "");

                    runOnUiThread(() -> {
                        if (!displayName.isEmpty()) {
                            etAddress.setText(displayName);
                            Toast.makeText(this, "Đã lấy địa chỉ thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không thể lấy địa chỉ", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
