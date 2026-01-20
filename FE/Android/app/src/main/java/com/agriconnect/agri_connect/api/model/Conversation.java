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

    // Additional fields from ConversationResponse
    @SerializedName("otherUserId")
    private String otherUserId;

    @SerializedName("otherUserName")
    private String otherUserName;

    @SerializedName("otherUserAvatar")
    private String otherUserAvatar;

    @SerializedName("unreadCount")
    private int unreadCount;

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

    public String getOtherUserId() {
        return otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public String getOtherUserAvatar() {
        return otherUserAvatar;
    }

    public int getUnreadCount() {
        return unreadCount;
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

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public void setOtherUserAvatar(String otherUserAvatar) {
        this.otherUserAvatar = otherUserAvatar;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    // Helper methods for compatibility
    public String getParticipantName() {
        return otherUserName;
    }

    public String getParticipantAvatarUrl() {
        return otherUserAvatar;
    }

    public String getLastMessageText() {
        return lastMessage != null ? lastMessage.getContent() : null;
    }

    public String getLastMessageTime() {
        return lastMessage != null ? lastMessage.getTime() : updatedAt;
    }
}
