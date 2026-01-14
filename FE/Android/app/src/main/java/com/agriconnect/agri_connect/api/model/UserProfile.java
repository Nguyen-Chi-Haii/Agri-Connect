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

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("kyc")
    private KycInfo kyc;

    @SerializedName("active")
    private boolean isActive;

    // Getters
    public String getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public String getRole() {
        return role;
    }

    public String getAvatar() {
        return avatar;
    }

    public KycInfo getKyc() {
        return kyc;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setKyc(KycInfo kyc) {
        this.kyc = kyc;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Compatibility method for old code
    public boolean isVerified() {
        return kyc != null && "VERIFIED".equals(kyc.getStatus());
    }
}
