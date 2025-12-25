package vn.agriconnect.API.dto.request.user;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String avatar;
    private String address;
}
