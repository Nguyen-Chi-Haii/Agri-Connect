package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for sending messages
 */
public class SendMessageRequest {
    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("recipientId")
    private String recipientId;

    @SerializedName("content")
    private String content;

    public SendMessageRequest(String conversationId, String recipientId, String content) {
        this.conversationId = conversationId;
        this.recipientId = recipientId;
        this.content = content;
    }

    // Getters and Setters
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
