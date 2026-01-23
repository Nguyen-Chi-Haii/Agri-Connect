package com.agriconnect.agri_connect.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.ui.auth.RoleSelectionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        // Load dashboard by default
        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_notification) {
            startActivity(new Intent(this, com.agriconnect.agri_connect.ui.notification.NotificationActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_admin_dashboard) {
                fragment = new AdminDashboardFragment();
            } else if (itemId == R.id.nav_admin_posts) {
                fragment = new AdminPostsFragment();
            } else if (itemId == R.id.nav_admin_categories) {
                fragment = new AdminCategoriesFragment();
            } else if (itemId == R.id.nav_admin_users) {
                fragment = new AdminUsersFragment();
            } else if (itemId == R.id.nav_admin_settings) {
                fragment = new AdminSettingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void logout() {
        ApiClient.getInstance(this).getTokenManager().clearTokens();
        ApiClient.resetInstance();

        Intent intent = new Intent(this, RoleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
