package com.agriconnect.agri_connect.ui.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.agriconnect.agri_connect.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class RoleSelectionActivity extends AppCompatActivity {

    private MaterialCardView cardFarmer;
    private MaterialCardView cardTrader;
    private ImageView ivFarmerCheck;
    private ImageView ivTraderCheck;
    private MaterialButton btnContinue;
    private TextView tvLoginLink;

    private String selectedRole = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role_selection);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        cardFarmer = findViewById(R.id.cardFarmer);
        cardTrader = findViewById(R.id.cardTrader);
        ivFarmerCheck = findViewById(R.id.ivFarmerCheck);
        ivTraderCheck = findViewById(R.id.ivTraderCheck);
        btnContinue = findViewById(R.id.btnContinue);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupListeners() {
        cardFarmer.setOnClickListener(v -> selectRole("FARMER"));
        cardTrader.setOnClickListener(v -> selectRole("TRADER"));

        btnContinue.setOnClickListener(v -> {
            if (selectedRole != null) {
                Intent intent = new Intent(this, CreateAccountActivity.class);
                intent.putExtra("role", selectedRole);
                startActivity(intent);
            }
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void selectRole(String role) {
        selectedRole = role;
        btnContinue.setEnabled(true);

        // Reset both cards
        resetCardStyle(cardFarmer, ivFarmerCheck);
        resetCardStyle(cardTrader, ivTraderCheck);

        // Highlight selected card
        if ("FARMER".equals(role)) {
            selectCardStyle(cardFarmer, ivFarmerCheck, R.color.farmer_green);
        } else {
            selectCardStyle(cardTrader, ivTraderCheck, R.color.trader_blue);
        }
    }

    private void resetCardStyle(MaterialCardView card, ImageView checkIcon) {
        card.setCardElevation(getResources().getDimension(R.dimen.elevation_sm));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.border));
        card.setStrokeWidth((int) getResources().getDimension(R.dimen.spacing_xs) / 4);
        checkIcon.setVisibility(View.GONE);
    }

    private void selectCardStyle(MaterialCardView card, ImageView checkIcon, int colorRes) {
        card.setCardElevation(getResources().getDimension(R.dimen.elevation_md));
        card.setStrokeColor(ContextCompat.getColor(this, colorRes));
        card.setStrokeWidth((int) getResources().getDimension(R.dimen.spacing_xs));
        checkIcon.setVisibility(View.VISIBLE);
    }
}
