package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Admin activity log model
 */
public class AdminLog {
    @SerializedName("id")
    private String id;

    @SerializedName("adminId")
    private String adminId;

    @SerializedName("action")
    private String action;

    @SerializedName("detail")
    private String detail;

    @SerializedName("timestamp")
    private String timestamp;

    public String getId() {
        return id;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
