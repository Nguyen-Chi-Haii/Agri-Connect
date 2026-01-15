package com.agriconnect.agri_connect.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.CategoryApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Category;
import com.agriconnect.agri_connect.ui.admin.adapter.CategoryAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoriesFragment extends Fragment {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private View progressBar, tvEmpty;
    private FloatingActionButton fabAdd;
    // No back button needed in fragment

    private CategoryApi categoryApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() != null) {
            categoryApi = ApiClient.getInstance(getContext()).getCategoryApi();
        }

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadCategories();
    }

    private void initViews(View view) {
        rvCategories = view.findViewById(R.id.rvCategories);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        fabAdd = view.findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter();
        adapter.setOnCategoryActionListener(new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                showAddEditDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                showDeleteConfirmation(category);
            }
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void loadCategories() {
        showLoading(true);
        if (categoryApi == null) return;

        categoryApi.getAllCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Category> categories = response.body().getData();
                    adapter.setCategories(categories);
                    tvEmpty.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    showError("Không thể tải danh sách danh mục");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showAddEditDialog(Category categoryToEdit) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_category, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etIcon = dialogView.findViewById(R.id.etIcon);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        View btnSave = dialogView.findViewById(R.id.btnSave);
        View btnCancel = dialogView.findViewById(R.id.btnCancel);

        if (categoryToEdit != null) {
            tvTitle.setText("Chỉnh sửa danh mục");
            etName.setText(categoryToEdit.getName());
            etIcon.setText(categoryToEdit.getIcon());
            etDescription.setText(categoryToEdit.getDescription());
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String icon = etIcon.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Vui lòng nhập tên");
                return;
            }

            if (icon.isEmpty()) {
                icon = "📦"; // Default icon
            }

            Category category = categoryToEdit != null ? categoryToEdit : new Category();
            category.setName(name);
            category.setIcon(icon);
            category.setDescription(description);

            showLoading(true);
            Callback<ApiResponse<Category>> callback = new Callback<ApiResponse<Category>>() {
                @Override
                public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                    showLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(),
                                categoryToEdit != null ? "Cập nhật thành công!" : "Thêm mới thành công!",
                                Toast.LENGTH_SHORT).show();
                        loadCategories();
                        dialog.dismiss();
                    } else {
                        showError("Thao tác thất bại");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {
                    showLoading(false);
                    showError("Lỗi: " + t.getMessage());
                }
            };

            if (categoryToEdit != null) {
                categoryApi.updateCategory(category.getId(), category).enqueue(callback);
            } else {
                categoryApi.createCategory(category).enqueue(callback);
            }
        });

        dialog.show();
    }

    private void showDeleteConfirmation(Category category) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    showLoading(true);
                    categoryApi.deleteCategory(category.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            showLoading(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                                loadCategories();
                            } else {
                                showError("Xóa thất bại");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            showLoading(false);
                            showError("Lỗi: " + t.getMessage());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
