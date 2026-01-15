package com.agriconnect.agri_connect.ui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.ui.main.MainNavigationActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EkycUploadActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvSkip;
    private TextInputEditText etIdNumber;
    private CardView cardIdFront, cardIdBack;
    private LinearLayout layoutIdFrontPlaceholder, layoutIdBackPlaceholder;
    private ImageView ivIdFront, ivIdBack;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;

    private Uri idFrontUri, idBackUri;
    private boolean isSelectingFront = true;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                if (isSelectingFront) {
                    idFrontUri = uri;
                    ivIdFront.setImageURI(uri);
                    ivIdFront.setVisibility(View.VISIBLE);
                    layoutIdFrontPlaceholder.setVisibility(View.GONE);
                } else {
                    idBackUri = uri;
                    ivIdBack.setImageURI(uri);
                    ivIdBack.setVisibility(View.VISIBLE);
                    layoutIdBackPlaceholder.setVisibility(View.GONE);
                }
                checkFormComplete();
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ekyc_upload);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvSkip = findViewById(R.id.tvSkip);
        etIdNumber = findViewById(R.id.etIdNumber);
        cardIdFront = findViewById(R.id.cardIdFront);
        cardIdBack = findViewById(R.id.cardIdBack);
        layoutIdFrontPlaceholder = findViewById(R.id.layoutIdFrontPlaceholder);
        layoutIdBackPlaceholder = findViewById(R.id.layoutIdBackPlaceholder);
        ivIdFront = findViewById(R.id.ivIdFront);
        ivIdBack = findViewById(R.id.ivIdBack);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvSkip.setOnClickListener(v -> navigateToMain());

        cardIdFront.setOnClickListener(v -> {
            isSelectingFront = true;
            pickImageLauncher.launch("image/*");
        });

        cardIdBack.setOnClickListener(v -> {
            isSelectingFront = false;
            pickImageLauncher.launch("image/*");
        });

        btnSubmit.setOnClickListener(v -> submitKyc());
    }

    private void checkFormComplete() {
        String idNumber = etIdNumber.getText() != null ? etIdNumber.getText().toString().trim() : "";
        boolean complete = !idNumber.isEmpty() && idFrontUri != null && idBackUri != null;
        btnSubmit.setEnabled(complete);
    }

    private void submitKyc() {
        String idNumber = etIdNumber.getText() != null ? etIdNumber.getText().toString().trim() : "";
        
        if (idNumber.isEmpty()) {
            etIdNumber.setError("Vui lòng nhập số CCCD");
            return;
        }

        // Validation: Must be 12 digits (Strict per user request)
        if (!idNumber.matches("^\\d{12}$")) {
            etIdNumber.setError("Số CCCD phải đủ 12 số");
            return;
        }

        if (idFrontUri == null || idBackUri == null) {
            Toast.makeText(this, "Vui lòng chọn đủ 2 mặt ảnh CCCD", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Upload Front Image
        uploadImage(idFrontUri, new UploadCallback() {
            @Override
            public void onSuccess(String frontUrl) {
                // Upload Back Image
                uploadImage(idBackUri, new UploadCallback() {
                    @Override
                    public void onSuccess(String backUrl) {
                        // Submit KYC
                        submitKycData(idNumber, frontUrl, backUrl);
                    }

                    @Override
                    public void onFailure(String message) {
                        showLoading(false);
                        Toast.makeText(EkycUploadActivity.this, "Lỗi upload ảnh mặt sau: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                showLoading(false);
                Toast.makeText(EkycUploadActivity.this, "Lỗi upload ảnh mặt trước: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitKycData(String idNumber, String frontUrl, String backUrl) {
        com.agriconnect.agri_connect.api.model.KycSubmissionRequest request = 
            new com.agriconnect.agri_connect.api.model.KycSubmissionRequest(idNumber, frontUrl, backUrl);

        com.agriconnect.agri_connect.api.ApiClient.getInstance(this).getUserApi().submitKyc(request)
            .enqueue(new retrofit2.Callback<com.agriconnect.agri_connect.api.model.ApiResponse<com.agriconnect.agri_connect.api.model.UserProfile>>() {
                @Override
                public void onResponse(retrofit2.Call<com.agriconnect.agri_connect.api.model.ApiResponse<com.agriconnect.agri_connect.api.model.UserProfile>> call, 
                                     retrofit2.Response<com.agriconnect.agri_connect.api.model.ApiResponse<com.agriconnect.agri_connect.api.model.UserProfile>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                         Toast.makeText(EkycUploadActivity.this, "Gửi xác minh thành công! Vui lòng chờ duyệt.", Toast.LENGTH_LONG).show();
                         navigateToMain();
                    } else {
                        Toast.makeText(EkycUploadActivity.this, "Gửi thất bại: " + (response.body() != null ? response.body().getMessage() : "Lỗi server"), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.agriconnect.agri_connect.api.model.ApiResponse<com.agriconnect.agri_connect.api.model.UserProfile>> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(EkycUploadActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    // --- Upload Helpers ---

    interface UploadCallback {
        void onSuccess(String url);
        void onFailure(String message);
    }

    private void uploadImage(Uri uri, UploadCallback callback) {
        try {
            java.io.File file = getFileFromUri(uri);
            if (file == null) {
                callback.onFailure("Không thể đọc file");
                return;
            }

            // Create RequestBody
            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), file);
            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            com.agriconnect.agri_connect.api.ApiClient.getInstance(this).getUploadApi().uploadFile(body, "kyc")
                .enqueue(new retrofit2.Callback<com.agriconnect.agri_connect.api.model.ApiResponse<java.util.Map<String, Object>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.agriconnect.agri_connect.api.model.ApiResponse<java.util.Map<String, Object>>> call, 
                                           retrofit2.Response<com.agriconnect.agri_connect.api.model.ApiResponse<java.util.Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                             java.util.Map<String, Object> data = response.body().getData();
                             String url = (String) data.get("secure_url");
                             if (url != null) {
                                 callback.onSuccess(url);
                             } else {
                                 callback.onFailure("Không nhận được URL ảnh");
                             }
                        } else {
                             callback.onFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.agriconnect.agri_connect.api.model.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                        callback.onFailure(t.getMessage());
                    }
                });

        } catch (Exception e) {
            callback.onFailure("Lỗi xử lý file: " + e.getMessage());
        }
    }

    private java.io.File getFileFromUri(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            java.io.File tempFile = java.io.File.createTempFile("upload", ".jpg", getCacheDir());
            java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            inputStream.close();
            return tempFile;
        } catch (java.io.IOException e) {
            return null;
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
        btnSubmit.setText(show ? "" : getString(R.string.submit_kyc));
    }
}
