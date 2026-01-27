package com.agriconnect.agri_connect.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Notification;
import com.agriconnect.agri_connect.ui.main.MainNavigationActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.rv_notifications);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        setupRecyclerView();
        loadNotifications();

        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this::onNotificationClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void onNotificationClick(Notification notification) {
        try {
            if (notification == null)
                return;
            if (notification.getId() == null)
                return;

            if (!notification.isRead()) {
                markAsRead(notification);
            }
        } catch (Exception e) {
            Log.e("Notification", "Error processing click", e);
        }
    }

    private void markAsRead(Notification notification) {
        if (getContext() == null)
            return;

        ApiClient.getInstance(getContext()).getNotificationApi().markAsRead(notification.getId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            notification.setRead(true);
                            adapter.notifyDataSetChanged();

                            // Update badge in parent activity
                            if (getActivity() instanceof MainNavigationActivity) {
                                ((MainNavigationActivity) getActivity()).fetchBadgeCounts(); // Helper to refresh badge
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("Notification", "Error marking as read", t);
                    }
                });
    }

    private void loadNotifications() {
        if (getContext() == null)
            return;

        swipeRefreshLayout.setRefreshing(true);
        ApiClient.getInstance(getContext()).getNotificationApi().getNotifications()
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

                            // Once loaded, we can clear the badge since the user has seen the list
                            if (getActivity() instanceof MainNavigationActivity) {
                                ((MainNavigationActivity) getActivity()).clearNotificationBadge();
                            }

                        } else {
                            if (getContext() != null)
                                Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                });
    }

    private void showEmptyState() {
        if (recyclerView != null)
            recyclerView.setVisibility(View.GONE);
        if (layoutEmpty != null)
            layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void showNotifications() {
        if (recyclerView != null)
            recyclerView.setVisibility(View.VISIBLE);
        if (layoutEmpty != null)
            layoutEmpty.setVisibility(View.GONE);
    }
}
