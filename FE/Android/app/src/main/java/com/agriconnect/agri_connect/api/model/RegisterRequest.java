package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Register request DTO
 */
public class RegisterRequest {
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("role")
    private String role;

    public RegisterRequest(String phone, String password, String fullName, String address, String role) {
        this.phone = phone;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
        this.role = role;
    }

    // Getters and setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
