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
            etIdNumber.setError("Vui lòng nhập số CMND/CCCD");
            return;
        }

        showLoading(true);

        // TODO: Upload images and call KYC API
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            Toast.makeText(this, "Đã gửi xác minh thành công!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        }, 2000);
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
