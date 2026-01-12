package com.agriconnect.agri_connect.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class AdminUsersFragment extends Fragment implements AdminUserAdapter.OnUserActionListener {

    private RecyclerView recyclerUsers;
    private TextView tvEmpty;
    private ProgressBar progressBar;

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
        loadUsers();
    }

    private void initViews(View view) {
        recyclerUsers = view.findViewById(R.id.recyclerUsers);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);

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

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        adminApi.getAllUsers().enqueue(new Callback<ApiResponse<List<UserProfile>>>() {
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
}
