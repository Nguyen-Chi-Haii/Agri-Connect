package com.agriconnect.agri_connect.api;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages authentication tokens using SharedPreferences
 */
public class TokenManager {
    private static final String PREF_NAME = "AgriConnectAuth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_KYC_STATUS = "kyc_status";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public void saveUserInfo(String userId, String userName, String role) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public void saveKycStatus(String kycStatus) {
        prefs.edit()
                .putString(KEY_KYC_STATUS, kycStatus)
                .apply();
    }

    public String getKycStatus() {
        return prefs.getString(KEY_KYC_STATUS, null);
    }

    public boolean isVerified() {
        String status = getKycStatus();
        return "VERIFIED".equals(status) || "APPROVED".equals(status);
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public void clearTokens() {
        prefs.edit().clear().apply();
    }

    public String getAuthHeader() {
        String token = getAccessToken();
        return token != null ? "Bearer " + token : null;
    }
}
