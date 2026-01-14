package com.agriconnect.agri_connect.ui.post;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.PostApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Location;
import com.agriconnect.agri_connect.api.model.Post;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialButton btnPost;
    private TextInputEditText etTitle, etContent, etPrice, etUnit, etQuantity, etLocation;
    private AutoCompleteTextView actvCategory;
    private LinearLayout layoutImages;
    private View btnAddImage;
    private ProgressBar progressBar;

    private PostApi postApi;
    private List<Uri> selectedImages = new ArrayList<>();
    private static final int MAX_IMAGES = 5;

    // Categories
    private String[] categories = {"Lúa gạo", "Rau củ", "Trái cây", "Thủy sản", "Gia súc", "Gia cầm", "Nông sản khác"};
    private String selectedCategoryId = null;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && selectedImages.size() < MAX_IMAGES) {
                    selectedImages.add(uri);
                    addImagePreview(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    pickImageLauncher.launch("image/*");
                } else {
                    Toast.makeText(this, "Cần quyền truy cập bộ nhớ để chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize API
        postApi = ApiClient.getInstance(this).getPostApi();

        initViews();
        setupCategoryDropdown();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPost = findViewById(R.id.btnPost);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        etPrice = findViewById(R.id.etPrice);
        etUnit = findViewById(R.id.etUnit);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        actvCategory = findViewById(R.id.actvCategory);
        layoutImages = findViewById(R.id.layoutImages);
        btnAddImage = findViewById(R.id.btnAddImage);
        progressBar = findViewById(R.id.progressBar);
    }

    private List<com.agriconnect.agri_connect.api.model.Category> categoryList = new ArrayList<>();

    private void setupCategoryDropdown() {
        // Fetch categories from API
        com.agriconnect.agri_connect.api.CategoryApi categoryApi = ApiClient.getInstance(this).getCategoryApi();
        categoryApi.getAllCategories().enqueue(new Callback<ApiResponse<List<com.agriconnect.agri_connect.api.model.Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.agriconnect.agri_connect.api.model.Category>>> call, 
                                   Response<ApiResponse<List<com.agriconnect.agri_connect.api.model.Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryList = response.body().getData();
                    List<String> categoryNames = new ArrayList<>();
                    for (com.agriconnect.agri_connect.api.model.Category cat : categoryList) {
                        // Display Icon + Name
                        String display = (cat.getIcon() != null ? cat.getIcon() + " " : "") + cat.getName();
                        categoryNames.add(display);
                    }
                    
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CreatePostActivity.this, 
                            android.R.layout.simple_dropdown_item_1line, categoryNames);
                    actvCategory.setAdapter(adapter);
                    
                    actvCategory.setOnItemClickListener((parent, view, position, id) -> {
                        selectedCategoryId = categoryList.get(position).getId();
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.agriconnect.agri_connect.api.model.Category>>> call, Throwable t) {
                Toast.makeText(CreatePostActivity.this, "Không thể tải danh mục: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddImage.setOnClickListener(v -> {
            if (selectedImages.size() >= MAX_IMAGES) {
                Toast.makeText(this, "Đã đạt số lượng ảnh tối đa (" + MAX_IMAGES + ")", Toast.LENGTH_SHORT).show();
                return;
            }
            pickImage();
        });

        btnPost.setOnClickListener(v -> createPost());
    }

    private void pickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*");
        } else {
            // For Android 13+, we don't need this permission for picking images
            pickImageLauncher.launch("image/*");
        }
    }

    private void addImagePreview(Uri uri) {
        View imageView = LayoutInflater.from(this).inflate(R.layout.item_image_preview, layoutImages, false);
        ImageView ivImage = imageView.findViewById(R.id.ivImage);
        ImageView btnRemove = imageView.findViewById(R.id.btnRemove);

        // Load image without Glide - use setImageURI
        ivImage.setImageURI(uri);

        btnRemove.setOnClickListener(v -> {
            selectedImages.remove(uri);
            layoutImages.removeView(imageView);
        });

        // Add before the "Add" button
        layoutImages.addView(imageView, layoutImages.getChildCount() - 1);
    }

    private void createPost() {
        String title = getText(etTitle);
        String content = getText(etContent);
        String priceStr = getText(etPrice);
        String unit = getText(etUnit);
        String quantityStr = getText(etQuantity);
        String location = getText(etLocation);
        
        // Find selected category if not set by click listener
        // This handles cases where user might type and select, or we rely on click listener. 
        // Best to rely on click listener or match string.
        String categoryText = actvCategory.getText().toString().trim();

        // Validate Category
        if (selectedCategoryId == null) {
             // Try to match text to category name
             for (com.agriconnect.agri_connect.api.model.Category cat : categoryList) {
                 String display = (cat.getIcon() != null ? cat.getIcon() + " " : "") + cat.getName();
                 if (display.equals(categoryText) || cat.getName().equals(categoryText)) {
                     selectedCategoryId = cat.getId();
                     break;
                 }
             }
        }

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            etTitle.requestFocus();
            return;
        }

        if (selectedCategoryId == null || categoryText.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Vui lòng nhập giá");
            etPrice.requestFocus();
            return;
        }

        showLoading(true);

        // Create post object
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(content);
        post.setCategoryId(selectedCategoryId);
        post.setCategoryName(categoryText); // Fallback name
        
        try {
            post.setPrice(Double.parseDouble(priceStr));
        } catch (NumberFormatException e) {
            post.setPrice(0.0);
        }
        post.setUnit(unit.isEmpty() ? "kg" : unit);
        if (!quantityStr.isEmpty()) {
            try {
                post.setQuantity(Double.parseDouble(quantityStr));
            } catch (NumberFormatException ignored) {}
        }
        
        Location loc = new Location();
        loc.setProvince(location); 
        post.setLocation(loc);
        
        // TODO: Upload images first
        
        postApi.createPost(post).enqueue(new Callback<ApiResponse<Post>>() {
            @Override
            public void onResponse(Call<ApiResponse<Post>> call, Response<ApiResponse<Post>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CreatePostActivity.this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String error = "Đăng bài thất bại";
                    if (response.body() != null && response.body().getMessage() != null) {
                        error = response.body().getMessage();
                    }
                    Toast.makeText(CreatePostActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Post>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CreatePostActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPost.setEnabled(!show);
    }
}
