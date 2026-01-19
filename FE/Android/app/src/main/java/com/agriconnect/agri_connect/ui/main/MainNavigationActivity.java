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

        // Badge will be updated when there are actual unread messages
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
        // Refresh badge count (demo - in real app, fetch from API)
        // updateChatBadge(getUnreadCount());
    }
}
