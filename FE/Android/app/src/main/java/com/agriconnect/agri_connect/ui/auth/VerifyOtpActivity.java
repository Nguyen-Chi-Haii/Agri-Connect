package com.agriconnect.agri_connect.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.agriconnect.agri_connect.R;
import com.google.android.material.button.MaterialButton;

public class VerifyOtpActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvPhone, tvTimer, tvResend;
    private EditText[] otpInputs;
    private MaterialButton btnVerify;
    private ProgressBar progressBar;

    private String phone;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_otp);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        phone = getIntent().getStringExtra("phone");

        initViews();
        setupViews();
        setupOtpInputs();
        startTimer();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPhone = findViewById(R.id.tvPhone);
        tvTimer = findViewById(R.id.tvTimer);
        tvResend = findViewById(R.id.tvResend);
        btnVerify = findViewById(R.id.btnVerify);
        progressBar = findViewById(R.id.progressBar);

        otpInputs = new EditText[]{
            findViewById(R.id.etOtp1),
            findViewById(R.id.etOtp2),
            findViewById(R.id.etOtp3),
            findViewById(R.id.etOtp4),
            findViewById(R.id.etOtp5),
            findViewById(R.id.etOtp6)
        };
    }

    private void setupViews() {
        // Format phone number for display
        if (phone != null && phone.length() >= 10) {
            String formatted = phone.substring(0, 4) + " " + 
                             phone.substring(4, 7) + " " + 
                             phone.substring(7);
            tvPhone.setText(formatted);
        }

        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> verifyOtp());

        tvResend.setOnClickListener(v -> {
            // TODO: Call resend OTP API
            startTimer();
            tvResend.setVisibility(View.GONE);
        });
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpInputs.length; i++) {
            final int index = i;
            EditText input = otpInputs[i];

            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].requestFocus();
                    }
                    checkOtpComplete();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            input.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && 
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    input.getText().length() == 0 && index > 0) {
                    otpInputs[index - 1].requestFocus();
                    otpInputs[index - 1].setText("");
                    return true;
                }
                return false;
            });
        }

        // Focus first input
        otpInputs[0].requestFocus();
    }

    private void checkOtpComplete() {
        boolean complete = true;
        for (EditText input : otpInputs) {
            if (input.getText().length() == 0) {
                complete = false;
                break;
            }
        }
        btnVerify.setEnabled(complete);
    }

    private String getOtpCode() {
        StringBuilder otp = new StringBuilder();
        for (EditText input : otpInputs) {
            otp.append(input.getText().toString());
        }
        return otp.toString();
    }

    private void verifyOtp() {
        String otp = getOtpCode();
        showLoading(true);

        // TODO: Call verify OTP API
        // For now, simulate success and navigate to eKYC
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            Intent intent = new Intent(this, EkycUploadActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            finish();
        }, 1500);
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText(String.format(getString(R.string.otp_expires_in), 
                    String.format("%02d:%02d", minutes, seconds)));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Mã đã hết hạn");
                tvResend.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!show);
        btnVerify.setText(show ? "" : getString(R.string.verify));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
