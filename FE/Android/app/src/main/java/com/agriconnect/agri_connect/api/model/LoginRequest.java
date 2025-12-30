package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Login request DTO
 */
public class LoginRequest {
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("password")
    private String password;

    public LoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
