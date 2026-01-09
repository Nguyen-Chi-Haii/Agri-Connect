package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * KYC (Know Your Customer) Information
 */
public class KycInfo {
    @SerializedName("cccd")
    private String cccd;

    @SerializedName("status")
    private String status;

    @SerializedName("rejectionReason")
    private String rejectionReason;

    public String getCccd() {
        return cccd;
    }

    public String getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
