package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Conversation model for chat
 */
public class Conversation {
    @SerializedName("id")
    private String id;
    
    @SerializedName("participantId")
    private String participantId;
    
    @SerializedName("participantName")
    private String participantName;
    
    @SerializedName("participantAvatarUrl")
    private String participantAvatarUrl;
    
    @SerializedName("lastMessage")
    private String lastMessage;
    
    @SerializedName("lastMessageTime")
    private String lastMessageTime;
    
    @SerializedName("unreadCount")
    private int unreadCount;

    // Getters
    public String getId() { return id; }
    public String getParticipantId() { return participantId; }
    public String getParticipantName() { return participantName; }
    public String getParticipantAvatarUrl() { return participantAvatarUrl; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public int getUnreadCount() { return unreadCount; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public void setParticipantAvatarUrl(String participantAvatarUrl) { this.participantAvatarUrl = participantAvatarUrl; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
