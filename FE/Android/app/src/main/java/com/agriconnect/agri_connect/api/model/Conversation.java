package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Conversation model for chat
 */
public class Conversation {
    @SerializedName("id")
    private String id;

    @SerializedName("participants")
    private List<String> participants;

    @SerializedName("lastMessage")
    private LastMessage lastMessage;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Getters
    public String getId() {
        return id;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public LastMessage getLastMessage() {
        return lastMessage;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public void setLastMessage(LastMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper for legacy code (will return null for now until backend or logic
    // provides it)
    public String getParticipantName() {
        return null;
    }

    public String getParticipantAvatarUrl() {
        return null;
    }

    public int getUnreadCount() {
        return 0;
    }

    public String getLastMessageText() {
        return lastMessage != null ? lastMessage.getContent() : null;
    }

    public String getLastMessageTime() {
        return lastMessage != null ? lastMessage.getTime() : updatedAt;
    }
}
