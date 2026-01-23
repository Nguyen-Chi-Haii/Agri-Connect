package com.agriconnect.agri_connect.ui.post;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.agriconnect.agri_connect.utils.FormValidator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private ImageView btnBack;
    private MaterialButton btnPost, btnGetLocation;
    private TextInputEditText etTitle, etContent, etPrice, etUnit, etQuantity, etLocation;
    private TextInputLayout tilTitle, tilContent, tilPrice, tilQuantity;
    private AutoCompleteTextView actvCategory;
    private LinearLayout layoutImages;
    private View btnAddImage;
    private ProgressBar progressBar;

    private PostApi postApi;
    private List<Uri> selectedImages = new ArrayList<>();
    private static final int MAX_IMAGES = 5;
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private String postId;
    private Post currentPost;

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    // Categories
    private List<com.agriconnect.agri_connect.api.model.Category> categoryList = new ArrayList<>();
    private String selectedCategoryId = null;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && selectedImages.size() < MAX_IMAGES) {
                    selectedImages.add(uri);
                    addImagePreview(uri);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    pickImageLauncher.launch("image/*");
                } else {
                    Toast.makeText(this, "Cần quyền truy cập bộ nhớ để chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Cần quyền truy cập vị trí để lấy địa chỉ", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get post ID from intent
        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy bài đăng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize API
        postApi = ApiClient.getInstance(this).getPostApi();

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupCategoryDropdown();
        setupListeners();
        loadPostData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPost = findViewById(R.id.btnPost);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        tilTitle = findViewById(R.id.tilTitle);
        tilContent = findViewById(R.id.tilContent);
        tilPrice = findViewById(R.id.tilPrice);
        tilQuantity = findViewById(R.id.tilQuantity);
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

    private void setupCategoryDropdown() {
        // Fetch categories from API
        com.agriconnect.agri_connect.api.CategoryApi categoryApi = ApiClient.getInstance(this).getCategoryApi();
        categoryApi.getAllCategories()
                .enqueue(new Callback<ApiResponse<List<com.agriconnect.agri_connect.api.model.Category>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<com.agriconnect.agri_connect.api.model.Category>>> call,
                            Response<ApiResponse<List<com.agriconnect.agri_connect.api.model.Category>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            categoryList = response.body().getData();
                            List<String> categoryNames = new ArrayList<>();
                            for (com.agriconnect.agri_connect.api.model.Category cat : categoryList) {
                                String display = (cat.getIcon() != null ? cat.getIcon() + " " : "") + cat.getName();
                                categoryNames.add(display);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditPostActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, categoryNames);
                            actvCategory.setAdapter(adapter);

                            actvCategory.setOnItemClickListener((parent, view, position, id) -> {
                                selectedCategoryId = categoryList.get(position).getId();
                            });

                            // After categories loaded, populate if post is loaded
                            if (currentPost != null) {
                                populateCategory();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<com.agriconnect.agri_connect.api.model.Category>>> call,
                            Throwable t) {
                        Toast.makeText(EditPostActivity.this, "Không thể tải danh mục: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupListeners() {
        // Real-time form validation
        FormValidator.addRequiredValidation(etTitle, tilTitle, "Tiêu đề");
        FormValidator.addRequiredValidation(etContent, tilContent, "Nội dung");
        FormValidator.addNumberValidation(etPrice, tilPrice, 1000, 10000000000.0, "Giá");
        FormValidator.addNumberValidation(etQuantity, tilQuantity, 0.1, 1000000, "Số lượng");

        btnBack.setOnClickListener(v -> finish());

        btnAddImage.setOnClickListener(v -> {
            if (selectedImages.size() >= MAX_IMAGES) {
                Toast.makeText(this, "Đã đạt số lượng ảnh tối đa (" + MAX_IMAGES + ")", Toast.LENGTH_SHORT).show();
                return;
            }
            pickImage();
        });

        btnPost.setOnClickListener(v -> updatePost());

        // Location button
        btnGetLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    private void loadPostData() {
        showLoading(true);
        
        postApi.getPostById(postId).enqueue(new Callback<ApiResponse<Post>>() {
            @Override
            public void onResponse(Call<ApiResponse<Post>> call, Response<ApiResponse<Post>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentPost = response.body().getData();
                    populateFields();
                } else {
                    Toast.makeText(EditPostActivity.this, "Không thể tải thông tin bài đăng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Post>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditPostActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields() {
        if (currentPost == null) return;

        etTitle.setText(currentPost.getTitle());
        etContent.setText(currentPost.getDescription());
        etPrice.setText(String.valueOf(currentPost.getPrice() != null ? currentPost.getPrice().intValue() : 0));
        etUnit.setText(currentPost.getUnit() != null ? currentPost.getUnit() : "kg");
        etQuantity.setText(String.valueOf(currentPost.getQuantity() != null ? currentPost.getQuantity() : 0));
        
        // Location
        if (currentPost.getLocation() != null) {
            etLocation.setText(currentPost.getLocation().toString());
        }

        // Category
        selectedCategoryId = currentPost.getCategoryId();
        populateCategory();

        // Images - Note: existing images stay as URLs, new images will be URIs
        // This is a simplified approach - just show that images exist
        if (currentPost.getImages() != null && !currentPost.getImages().isEmpty()) {
            // For simplicity, we won't display existing images in edit mode
            // User must select images again if they want to change them
            Toast.makeText(this, "Bài đăng hiện có " + currentPost.getImages().size() + " ảnh. Chọn ảnh mới để thay thế.", Toast.LENGTH_LONG).show();
        }
    }

    private void populateCategory() {
        if (selectedCategoryId == null || categoryList.isEmpty()) return;

        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(selectedCategoryId)) {
                String display = (categoryList.get(i).getIcon() != null ? categoryList.get(i).getIcon() + " " : "") 
                        + categoryList.get(i).getName();
                actvCategory.setText(display, false);
                break;
            }
        }
    }

    private boolean isLocationInVietnam(double latitude, double longitude) {
        return latitude >= 8.0 && latitude <= 24.0 &&
                longitude >= 102.0 && longitude <= 110.0;
    }

    private void getCurrentLocation() {
        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Đang lấy...");

        double defaultLat = 10.762622;
        double defaultLng = 106.660172;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Dùng vị trí mặc định (TP.HCM)", Toast.LENGTH_SHORT).show();
            getAddressFromLocation(defaultLat, defaultLng);
            return;
        }

        android.os.Handler handler = new android.os.Handler(getMainLooper());
        final boolean[] locationReceived = { false };

        handler.postDelayed(() -> {
            if (!locationReceived[0]) {
                locationReceived[0] = true;
                btnGetLocation.setEnabled(true);
                btnGetLocation.setText("Lấy vị trí");
                Toast.makeText(this, "Không nhận được tín hiệu GPS, dùng vị trí mặc định (TP.HCM)", Toast.LENGTH_SHORT)
                        .show();
                getAddressFromLocation(defaultLat, defaultLng);
            }
        }, 3000);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (!locationReceived[0]) {
                        locationReceived[0] = true;
                        btnGetLocation.setEnabled(true);
                        btnGetLocation.setText("Lấy vị trí");

                        if (location != null) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();

                            if (isLocationInVietnam(lat, lng)) {
                                Toast.makeText(this, "Đang lấy địa chỉ từ GPS...", Toast.LENGTH_SHORT).show();
                                getAddressFromLocation(lat, lng);
                            } else {
                                Toast.makeText(this, "Vị trí GPS không thuộc Việt Nam, dùng vị trí mặc định (TP.HCM)",
                                        Toast.LENGTH_LONG).show();
                                getAddressFromLocation(defaultLat, defaultLng);
                            }
                        } else {
                            Toast.makeText(this, "Không lấy được vị trí, dùng vị trí mặc định (TP.HCM)",
                                    Toast.LENGTH_SHORT).show();
                            getAddressFromLocation(defaultLat, defaultLng);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!locationReceived[0]) {
                        locationReceived[0] = true;
                        btnGetLocation.setEnabled(true);
                        btnGetLocation.setText("Lấy vị trí");
                        Toast.makeText(this, "Lỗi GPS, dùng vị trí mặc định (TP.HCM)", Toast.LENGTH_SHORT).show();
                        getAddressFromLocation(defaultLat, defaultLng);
                    }
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        new Thread(() -> {
            try {
                String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                        + latitude + "&lon=" + longitude + "&accept-language=vi";

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .header("User-Agent", "AgriConnect/1.0")
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    org.json.JSONObject jsonObject = new org.json.JSONObject(json);
                    String displayName = jsonObject.optString("display_name", "");

                    runOnUiThread(() -> {
                        if (!displayName.isEmpty()) {
                            etLocation.setText(displayName);
                            Toast.makeText(this, "Đã lấy địa chỉ thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không thể lấy địa chỉ", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void pickImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*");
        } else {
            pickImageLauncher.launch("image/*");
        }
    }

    private void addImagePreview(Uri uri) {
        View imageView = LayoutInflater.from(this).inflate(R.layout.item_image_preview, layoutImages, false);
        ImageView ivImage = imageView.findViewById(R.id.ivImage);
        ImageView btnRemove = imageView.findViewById(R.id.btnRemove);

        ivImage.setImageURI(uri);

        btnRemove.setOnClickListener(v -> {
            selectedImages.remove(uri);
            layoutImages.removeView(imageView);
        });

        layoutImages.addView(imageView, layoutImages.getChildCount() - 1);
    }

    private void updatePost() {
        String title = getText(etTitle);
        String content = getText(etContent);
        String priceStr = getText(etPrice);
        String unit = getText(etUnit);
        String quantityStr = getText(etQuantity);
        String location = getText(etLocation);

        String categoryText = actvCategory.getText().toString().trim();

        if (selectedCategoryId == null) {
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

        // Create post object with updated data
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(content);
        post.setCategoryId(selectedCategoryId);
        post.setCategoryName(categoryText);

        try {
            post.setPrice(Double.parseDouble(priceStr));
        } catch (NumberFormatException e) {
            post.setPrice(0.0);
        }
        post.setUnit(unit.isEmpty() ? "kg" : unit);
        if (!quantityStr.isEmpty()) {
            try {
                post.setQuantity(Double.parseDouble(quantityStr));
            } catch (NumberFormatException ignored) {
            }
        }

        Location loc = new Location();
        loc.setProvince(location);
        post.setLocation(loc);

        // Handle images
        if (!selectedImages.isEmpty()) {
            // User selected new images - convert and add them
            List<String> base64Images = getImagesAsBase64();
            post.setImages(base64Images);
            android.util.Log.d("EditPost", "New images to upload: " + base64Images.size());
        } else {
            // No new images selected - keep existing images
            if (currentPost != null && currentPost.getImages() != null) {
                post.setImages(currentPost.getImages());
                android.util.Log.d("EditPost", "Keeping existing images: " + currentPost.getImages().size());
            }
        }

        postApi.updatePost(postId, post).enqueue(new Callback<ApiResponse<Post>>() {
            @Override
            public void onResponse(Call<ApiResponse<Post>> call, Response<ApiResponse<Post>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(EditPostActivity.this, "Cập nhật bài đăng thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String error = "Cập nhật thất bại";
                    if (response.body() != null && response.body().getMessage() != null) {
                        error = response.body().getMessage();
                    }
                    Toast.makeText(EditPostActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Post>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditPostActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private String convertUriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null)
                return null;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null)
                return null;

            int maxWidth = 800;
            if (bitmap.getWidth() > maxWidth) {
                int newHeight = (int) ((float) maxWidth / bitmap.getWidth() * bitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> getImagesAsBase64() {
        List<String> base64Images = new ArrayList<>();
        for (Uri uri : selectedImages) {
            String base64 = convertUriToBase64(uri);
            if (base64 != null) {
                base64Images.add(base64);
            }
        }
        return base64Images;
    }
}
