package com.agriconnect.agri_connect.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.PostApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.PagedResponse;
import com.agriconnect.agri_connect.api.model.Post;
import com.agriconnect.agri_connect.ui.home.HomeFragment;
import com.agriconnect.agri_connect.ui.home.PostAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextInputEditText etSearch;
    private ChipGroup chipGroup;
    private RecyclerView rvResults;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private ProgressBar progressBar;

    private PostAdapter postAdapter;
    private PostApi postApi;
    private Timer searchTimer;
    private String currentCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postApi = ApiClient.getInstance(this).getPostApi();

        initViews();
        setupRecyclerView();
        setupListeners();
        
        // Focus search and show keyboard
        etSearch.requestFocus();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        chipGroup = findViewById(R.id.chipGroup);
        rvResults = findViewById(R.id.rvResults);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter();
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(postAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Search on text change with debounce
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> performSearch());
                    }
                }, 500); // 500ms debounce
            }
        });

        // Search on keyboard action
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Category filter
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = null;
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null && chip.getId() != R.id.chipAll) {
                    currentCategory = chip.getText().toString();
                } else {
                    currentCategory = null;
                }
            }
            performSearch();
        });
    }

    private void performSearch() {
        String keyword = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";

        if (keyword.isEmpty() && currentCategory == null) {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("Nhập từ khóa để tìm kiếm");
            rvResults.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        postApi.searchPosts(keyword, currentCategory, null, null).enqueue(new Callback<ApiResponse<PagedResponse<Post>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<Post>>> call, Response<ApiResponse<PagedResponse<Post>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PagedResponse<Post> pagedResponse = response.body().getData();
                    List<Post> posts = pagedResponse != null ? pagedResponse.getContent() : null;

                    if (posts != null && !posts.isEmpty()) {
                        List<HomeFragment.PostItem> postItems = new ArrayList<>();
                        for (Post post : posts) {
                            HomeFragment.PostItem item = new HomeFragment.PostItem(
                                    post.getId(),
                                    post.getSellerName(),
                                    formatTime(post.getCreatedAt()),
                                    post.getDescription() != null ? post.getDescription() : post.getTitle(),
                                    formatPrice(post.getPrice(), post.getUnit()),
                                    0, 0, post.getViewCount(),
                                    post.isSellerVerified(),
                                    post.getStatus()
                            );
                            if (post.getImages() != null && !post.getImages().isEmpty()) {
                                item.imageUrl = post.getImages().get(0);
                                item.imageCount = post.getImages().size();
                            } else {
                                item.imageCount = 0;
                            }
                            postItems.add(item);
                        }

                        postAdapter.setData(postItems);
                        rvResults.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                    } else {
                        showEmptyResults();
                    }
                } else {
                    showEmptyResults();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<Post>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Lỗi tìm kiếm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyResults();
            }
        });
    }

    private void showEmptyResults() {
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText("Không tìm thấy kết quả");
        rvResults.setVisibility(View.GONE);
    }

    private String formatTime(String createdAt) {
        if (createdAt == null) return "";
        return createdAt.substring(0, Math.min(10, createdAt.length()));
    }

    private String formatPrice(Double price, String unit) {
        if (price == null) return null;
        String priceStr = String.format("%,.0f", price) + "đ";
        if (unit != null) {
            priceStr += "/" + unit;
        }
        return priceStr;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchTimer != null) {
            searchTimer.cancel();
        }
    }
}
