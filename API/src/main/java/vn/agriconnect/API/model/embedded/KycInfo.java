package vn.agriconnect.API.model.embedded;

import lombok.Data;

/**
 * KYC (Know Your Customer) Information - Embedded Document
 */
@Data
public class KycInfo {
    private String cccd;
    private String cccdFrontImage;
    private String cccdBackImage;
    private String status; // PENDING, VERIFIED, REJECTED
    private String rejectionReason;
}
