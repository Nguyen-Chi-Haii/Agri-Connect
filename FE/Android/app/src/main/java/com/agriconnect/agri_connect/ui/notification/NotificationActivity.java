package com.agriconnect.agri_connect.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Notification;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Remove back arrow to make it look like an independent activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.rv_notifications);
        layoutEmpty = findViewById(R.id.layout_empty);

        setupRecyclerView();
        loadNotifications();

        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this::onNotificationClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void onNotificationClick(Notification notification) {
        try {
            if (notification == null) {
                Log.e("Notification", "Clicked notification is null");
                return;
            }

            if (notification.getId() == null) {
                Log.e("Notification", "Notification ID is null for read mark");
                // Still allow UI interaction if needed, but skip API call
                return;
            }

            if (!notification.isRead()) {
                markAsRead(notification);
            } else {
                // Already read
            }
        } catch (Exception e) {
            Log.e("Notification", "Error processing notification click", e);
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void markAsRead(Notification notification) {
        ApiClient.getInstance(this).getNotificationApi().markAsRead(notification.getId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            notification.setRead(true);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("Notification", "Error marking as read", t);
                    }
                });
    }

    private void loadNotifications() {
        swipeRefreshLayout.setRefreshing(true);
        ApiClient.getInstance(this).getNotificationApi().getNotifications()
                .enqueue(new Callback<ApiResponse<List<Notification>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Notification>>> call,
                            Response<ApiResponse<List<Notification>>> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Notification> data = response.body().getData();
                            if (data != null && !data.isEmpty()) {
                                adapter.setNotifications(data);
                                showNotifications();
                            } else {
                                adapter.setNotifications(new ArrayList<>());
                                showEmptyState();
                            }
                        } else {
                            Toast.makeText(NotificationActivity.this, "Failed to load notifications",
                                    Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(NotificationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                        showEmptyState();
                    }
                });
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void showNotifications() {
        recyclerView.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
