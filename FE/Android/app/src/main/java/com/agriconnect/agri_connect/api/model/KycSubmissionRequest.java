package com.agriconnect.agri_connect.api.model;

public class KycSubmissionRequest {
    private String idNumber;
    private String idFrontImage;
    private String idBackImage;
    private String selfieImage;

    public KycSubmissionRequest(String idNumber, String idFrontImage, String idBackImage) {
        this.idNumber = idNumber;
        this.idFrontImage = idFrontImage;
        this.idBackImage = idBackImage;
    }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getIdFrontImage() { return idFrontImage; }
    public void setIdFrontImage(String idFrontImage) { this.idFrontImage = idFrontImage; }

    public String getIdBackImage() { return idBackImage; }
    public void setIdBackImage(String idBackImage) { this.idBackImage = idBackImage; }

    public String getSelfieImage() { return selfieImage; }
    public void setSelfieImage(String selfieImage) { this.selfieImage = selfieImage; }
}
