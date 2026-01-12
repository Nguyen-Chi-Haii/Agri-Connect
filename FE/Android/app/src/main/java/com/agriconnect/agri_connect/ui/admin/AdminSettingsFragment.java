package com.agriconnect.agri_connect.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.google.android.material.button.MaterialButton;

public class AdminSettingsFragment extends Fragment {

    private TextView tvAdminName, tvAvatar;
    private MaterialButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadAdminInfo();
    }

    private void initViews(View view) {
        tvAdminName = view.findViewById(R.id.tvAdminName);
        tvAvatar = view.findViewById(R.id.tvAvatar);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).logout();
            }
        });
    }

    private void loadAdminInfo() {
        if (getContext() != null) {
            String adminName = ApiClient.getInstance(getContext()).getTokenManager().getUserName();
            if (adminName != null) {
                tvAdminName.setText(adminName);
                tvAvatar.setText(String.valueOf(adminName.charAt(0)).toUpperCase());
            }
        }
    }
}
