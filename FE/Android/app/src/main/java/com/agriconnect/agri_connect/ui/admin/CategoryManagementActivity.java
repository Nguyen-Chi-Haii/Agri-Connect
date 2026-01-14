package com.agriconnect.agri_connect.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class CategoryManagementActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private View progressBar, tvEmpty;
    private FloatingActionButton fabAdd;
    private View btnBack;

    private CategoryApi categoryApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoryApi = ApiClient.getInstance(this).getCategoryApi();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadCategories();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAdd = findViewById(R.id.fabAdd);
        btnBack = findViewById(R.id.btnBack);
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
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void loadCategories() {
        showLoading(true);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_category, null);
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
                        Toast.makeText(CategoryManagementActivity.this, 
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
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    showLoading(true);
                    categoryApi.deleteCategory(category.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            showLoading(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(CategoryManagementActivity.this, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
