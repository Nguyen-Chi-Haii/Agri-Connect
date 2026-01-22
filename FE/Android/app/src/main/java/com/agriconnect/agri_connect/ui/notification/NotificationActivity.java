package com.agriconnect.agri_connect.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.rv_notifications);
        
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
        if (!notification.isRead()) {
            markAsRead(notification);
        } else {
            // Already read, maybe navigate to detail?
            // For now just show toast or do nothing
            // Toast.makeText(this, notification.getContent(), Toast.LENGTH_SHORT).show();
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
                    public void onResponse(Call<ApiResponse<List<Notification>>> call, Response<ApiResponse<List<Notification>>> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Notification> data = response.body().getData();
                            if (data != null) {
                                adapter.setNotifications(data);
                            } else {
                                adapter.setNotifications(new ArrayList<>());
                            }
                        } else {
                            Toast.makeText(NotificationActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(NotificationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
