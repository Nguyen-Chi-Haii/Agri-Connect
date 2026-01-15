package com.agriconnect.agri_connect.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.AdminApi;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.UserProfile;
import com.agriconnect.agri_connect.ui.admin.adapter.AdminUserAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.widget.SearchView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class AdminUsersFragment extends Fragment implements AdminUserAdapter.OnUserActionListener {

    private RecyclerView recyclerUsers;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ChipGroup chipGroupFilter;

    private AdminUserAdapter adapter;
    private AdminApi adminApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadUsers();
    }

    private void initViews(View view) {
        recyclerUsers = view.findViewById(R.id.recyclerUsers);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        searchView = view.findViewById(R.id.searchView);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);

        if (getContext() != null) {
            adminApi = ApiClient.getInstance(getContext()).getAdminApi();
        }
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter();
        adapter.setOnUserActionListener(this);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadUsers();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: Debounce here if needed, for now load immediately or on submit
                // Let's load immediately for responsive feel, maybe check length
                loadUsers();
                return true;
            }
        });

        // Use standard OnCheckedChangeListener for simple ChipGroups (singleSelection=false)
        // Or for material ChipGroup, we can use setOnCheckedStateChangeListener in newer versions but old way is valid too
        // Let's iterate chips in loadUsers(), so just trigger load on any click
        for (int i = 0; i < chipGroupFilter.getChildCount(); i++) {
            View child = chipGroupFilter.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setOnCheckedChangeListener((buttonView, isChecked) -> loadUsers());
            }
        }
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        String search = searchView.getQuery().toString();
        if (search.trim().isEmpty()) search = null;

        String role = null;
        String kycStatus = null;

        if (chipGroupFilter != null) {
            Chip chipRoleFarmer = chipGroupFilter.findViewById(R.id.chipRoleFarmer);
            Chip chipRoleTrader = chipGroupFilter.findViewById(R.id.chipRoleTrader);
            Chip chipKycVerified = chipGroupFilter.findViewById(R.id.chipKycVerified);
            Chip chipKycPending = chipGroupFilter.findViewById(R.id.chipKycPending);

            if (chipRoleFarmer != null && chipRoleFarmer.isChecked()) role = "FARMER";
            else if (chipRoleTrader != null && chipRoleTrader.isChecked()) role = "TRADER";

            if (chipKycVerified != null && chipKycVerified.isChecked()) kycStatus = "VERIFIED";
            else if (chipKycPending != null && chipKycPending.isChecked()) kycStatus = "PENDING";
        }

        adminApi.getAllUsers(search, role, kycStatus).enqueue(new Callback<ApiResponse<List<UserProfile>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserProfile>>> call,
                    Response<ApiResponse<List<UserProfile>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<UserProfile> users = response.body().getData();
                    adapter.setUsers(users);

                    boolean isEmpty = users == null || users.isEmpty();
                    tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    recyclerUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserProfile>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onVerifyKyc(UserProfile user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác minh KYC")
                .setMessage("Xác nhận xác minh KYC cho " + user.getFullName() + "?")
                .setPositiveButton("Xác minh", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    adminApi.verifyKyc(user.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã xác minh KYC", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onRejectKyc(UserProfile user) {
        EditText input = new EditText(getContext());
        input.setHint("Lý do từ chối");

        new AlertDialog.Builder(getContext())
                .setTitle("Từ chối KYC")
                .setView(input)
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    String reason = input.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);

                    adminApi.rejectKyc(user.getId(), reason).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã từ chối KYC", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    @Override
    public void onLockUser(UserProfile user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Khóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn khóa tài khoản " + user.getFullName() + "?\nNgười dùng này sẽ không thể đăng nhập hoặc thực hiện hành động nào.")
                .setPositiveButton("Khóa", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    adminApi.lockUser(user.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã khóa tài khoản", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(getContext(), "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onUnlockUser(UserProfile user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Mở khóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn mở khóa tài khoản " + user.getFullName() + "?")
                .setPositiveButton("Mở khóa", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    adminApi.unlockUser(user.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã mở khóa tài khoản", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(getContext(), "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    @Override
    public void onUserClick(UserProfile user) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_user_detail, null);

        // Bind data
        TextView tvAvatar = view.findViewById(R.id.tvDetailAvatar);
        TextView tvFullName = view.findViewById(R.id.tvDetailFullName);
        TextView tvRole = view.findViewById(R.id.tvDetailRole);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvPhone = view.findViewById(R.id.tvDetailPhone);
        TextView tvAddress = view.findViewById(R.id.tvDetailAddress);
        
        LinearLayout layoutTaxCode = view.findViewById(R.id.layoutTaxCode);
        TextView tvTaxCode = view.findViewById(R.id.tvTaxCode);

        TextView tvKycStatus = view.findViewById(R.id.tvDetailKycStatus);
        TextView tvCccd = view.findViewById(R.id.tvDetailCccd);
        View btnClose = view.findViewById(R.id.btnCloseDialog);

        // Name & Avatar
        String name = user.getFullName();
        if (name != null && !name.isEmpty()) {
            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
            tvFullName.setText(name);
        } else {
            tvAvatar.setText("U");
            tvFullName.setText("Chưa cập nhật");
        }

        // Role
        String role = user.getRole();
        if ("ADMIN".equals(role)) tvRole.setText("Quản trị viên");
        else if ("FARMER".equals(role)) tvRole.setText("Nông dân");
        else if ("TRADER".equals(role)) tvRole.setText("Thương lái");
        else tvRole.setText("Người dùng");

        // Status
        if (user.isActive()) {
            tvStatus.setVisibility(View.GONE);
        } else {
            tvStatus.setVisibility(View.VISIBLE);
        }

        // Contact
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật");
        tvAddress.setText(user.getAddress() != null ? user.getAddress() : "Chưa cập nhật");

        // Tax Code (Trader only)
        if ("TRADER".equals(user.getRole()) && user.getKyc() != null && user.getKyc().getTaxCode() != null) {
            layoutTaxCode.setVisibility(View.VISIBLE);
            tvTaxCode.setText(user.getKyc().getTaxCode());
        } else {
            layoutTaxCode.setVisibility(View.GONE);
        }

        // KYC Info
        if (user.getKyc() != null) {
            String status = user.getKyc().getStatus();
            if ("VERIFIED".equals(status)) {
                tvKycStatus.setText("Đã xác minh");
                tvKycStatus.setTextColor(getResources().getColor(R.color.success));
            } else if ("PENDING".equals(status)) {
                tvKycStatus.setText("Chờ duyệt");
                tvKycStatus.setTextColor(getResources().getColor(R.color.warning));
            } else if ("REJECTED".equals(status)) {
                tvKycStatus.setText("Đã từ chối");
                tvKycStatus.setTextColor(getResources().getColor(R.color.error));
            }
            
            if (user.getKyc().getCccd() != null) {
                tvCccd.setVisibility(View.VISIBLE);
                tvCccd.setText("CCCD: " + user.getKyc().getCccd());
            } else {
                tvCccd.setVisibility(View.GONE);
            }
        } else {
            tvKycStatus.setText("Chưa gửi KYC");
            tvKycStatus.setTextColor(getResources().getColor(R.color.text_hint));
            tvCccd.setVisibility(View.GONE);
        }

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    @Override
    public void onViewImage(String imageUrl) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        // Create a layout for the dialog (ImageView with a Close button)
        android.widget.RelativeLayout layout = new android.widget.RelativeLayout(getContext());
        layout.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(android.graphics.Color.BLACK);

        // ImageView
        // Use standard ImageView with fitCenter
        android.widget.ImageView standardImageView = new android.widget.ImageView(getContext());
        standardImageView.setLayoutParams(new android.widget.RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        standardImageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        
        layout.addView(standardImageView);

        // Close Button
        android.widget.ImageButton closeButton = new android.widget.ImageButton(getContext());
        android.widget.RelativeLayout.LayoutParams closeParams = new android.widget.RelativeLayout.LayoutParams(
                dpToPx(48), dpToPx(48));
        closeParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP);
        closeParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
        closeParams.setMargins(0, dpToPx(16), dpToPx(16), 0);
        closeButton.setLayoutParams(closeParams);
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeButton.setBackgroundResource(android.R.color.transparent);
        closeButton.setColorFilter(android.graphics.Color.WHITE);
        
        layout.addView(closeButton);

        builder.setView(layout);
        AlertDialog dialog = builder.create();
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        // Load image
        com.bumptech.glide.Glide.with(getContext())
                .load(imageUrl)
                .into(standardImageView);

        dialog.show();
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
