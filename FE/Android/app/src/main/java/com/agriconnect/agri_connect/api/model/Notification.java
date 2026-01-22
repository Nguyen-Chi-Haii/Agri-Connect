package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("type")
    private String type; // NEW_MESSAGE, POST_APPROVED, etc.

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("read")
    private boolean read;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
