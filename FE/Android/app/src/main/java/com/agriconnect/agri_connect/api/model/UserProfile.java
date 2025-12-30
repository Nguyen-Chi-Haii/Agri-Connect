package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * User profile response
 */
public class UserProfile {
    @SerializedName("id")
    private String id;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("avatarUrl")
    private String avatarUrl;
    
    @SerializedName("kycStatus")
    private String kycStatus;
    
    @SerializedName("isVerified")
    private boolean isVerified;
    
    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public String getId() { return id; }
    public String getPhone() { return phone; }
    public String getFullName() { return fullName; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getKycStatus() { return kycStatus; }
    public boolean isVerified() { return isVerified; }
    public String getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setAddress(String address) { this.address = address; }
    public void setRole(String role) { this.role = role; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
