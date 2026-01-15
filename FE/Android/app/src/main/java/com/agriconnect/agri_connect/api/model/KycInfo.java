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

    @SerializedName("taxCode")
    private String taxCode;
    @SerializedName("cccdFrontImage")
    private String cccdFrontImage;
    @SerializedName("cccdBackImage")
    private String cccdBackImage;

    public String getCccd() {
        return cccd;
    }

    public String getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public String getCccdFrontImage() {
        return cccdFrontImage;
    }

    public String getCccdBackImage() {
        return cccdBackImage;
    }
}
