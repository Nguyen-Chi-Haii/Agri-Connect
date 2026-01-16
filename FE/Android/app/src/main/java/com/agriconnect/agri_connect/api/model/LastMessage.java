package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Last message summary in a conversation
 */
public class LastMessage {
    @SerializedName("content")
    private String content;

    @SerializedName("type")
    private String type;

    @SerializedName("time")
    private String time;

    @SerializedName("senderId")
    private String senderId;

    // Getters
    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public String getSenderId() {
        return senderId;
    }

    // Setters
    public void setContent(String content) {
        this.content = content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
