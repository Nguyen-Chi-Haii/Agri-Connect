package com.agriconnect.agri_connect.ui.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.ui.chat.ChatListFragment;
import com.agriconnect.agri_connect.ui.home.HomeFragment;
import com.agriconnect.agri_connect.ui.market.MarketFragment;
import com.agriconnect.agri_connect.ui.profile.ProfileFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainNavigationActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private BadgeDrawable chatBadge;
    private BadgeDrawable notificationBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_navigation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Check for Admin redirection
        ApiClient apiClient = ApiClient.getInstance(this);
        com.agriconnect.agri_connect.api.TokenManager tokenManager = apiClient.getTokenManager();
        if (tokenManager.isLoggedIn() && "ADMIN".equals(tokenManager.getUserRole())) {
             android.content.Intent intent = new android.content.Intent(this, com.agriconnect.agri_connect.ui.admin.AdminMainActivity.class);
             intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
             startActivity(intent);
             finish();
             return;
        }

        initViews();
        setupNavigation();
        setupBadges();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_market) {
                fragment = new MarketFragment();
            } else if (itemId == R.id.nav_chat) {
                fragment = new ChatListFragment();
                // Clear badge when entering chat
                clearChatBadge();
            } else if (itemId == R.id.nav_notification) {
                startActivity(new android.content.Intent(this, com.agriconnect.agri_connect.ui.notification.NotificationActivity.class));
                // Clear badge when viewing notifications
                clearNotificationBadge();
                return true;
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void setupBadges() {
        // Create chat badge
        chatBadge = bottomNavigation.getOrCreateBadge(R.id.nav_chat);
        chatBadge.setVisible(false);

        // Create notification badge
        notificationBadge = bottomNavigation.getOrCreateBadge(R.id.nav_notification);
        notificationBadge.setVisible(false);

        // Badge will be updated when there are actual unread messages/notifications
        // (This should be called from a WebSocket listener or API fetch)
    }

    /**
     * Update the chat badge with unread message count
     * 
     * @param count Number of unread messages
     */
    public void updateChatBadge(int count) {
        if (count > 0) {
            chatBadge.setNumber(count);
            chatBadge.setVisible(true);
        } else {
            chatBadge.setVisible(false);
        }
    }

    /**
     * Update the notification badge with unread count
     * 
     * @param count Number of unread notifications
     */
    public void updateNotificationBadge(int count) {
        if (count > 0) {
            notificationBadge.setNumber(count);
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }
    }

    /**
     * Clear the notification badge
     */
    public void clearNotificationBadge() {
        notificationBadge.setVisible(false);
    }

    /**
     * Clear the chat badge
     */
    public void clearChatBadge() {
        chatBadge.setVisible(false);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    // Exit confirmation
    private long backPressedTime = 0;
    private android.widget.Toast backToast;

    @Override
    public void onBackPressed() {
        // Check if we're on home fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (currentFragment instanceof HomeFragment) {
            // On home - confirm exit
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                if (backToast != null)
                    backToast.cancel();
                super.onBackPressed();
                return;
            } else {
                backToast = android.widget.Toast.makeText(this,
                        "Nhấn lần nữa để thoát ứng dụng", android.widget.Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        } else {
            // Not on home - navigate to home
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchBadgeCounts();
    }

    /**
     * Fetch badge counts from API
     */
    private void fetchBadgeCounts() {
        // Fetch notification count
        ApiClient.getInstance(this).getNotificationApi()
                .getUnreadCount()
                .enqueue(new retrofit2.Callback<com.agriconnect.agri_connect.api.model.ApiResponse<Long>>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.agriconnect.agri_connect.api.model.ApiResponse<Long>> call,
                            retrofit2.Response<com.agriconnect.agri_connect.api.model.ApiResponse<Long>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Long count = response.body().getData();
                            updateNotificationBadge(count.intValue());
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<com.agriconnect.agri_connect.api.model.ApiResponse<Long>> call,
                            Throwable t) {
                        // Silently fail - badge just won't update
                    }
                });

        // TODO: Fetch chat unread count when API is available
        // For now, chat badge stays hidden
    }
}
