package vn.agriconnect.API.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycSubmissionRequest {
    @NotBlank(message = "ID number is required")
    private String idNumber;
    
    @NotBlank(message = "ID front image is required")
    private String idFrontImage;
    
    @NotBlank(message = "ID back image is required")
    private String idBackImage;
    
    private String selfieImage;
}
