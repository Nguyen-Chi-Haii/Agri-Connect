package vn.agriconnect.API.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.agriconnect.API.model.enums.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String phone;
    private String fullName;
    private String avatar;
    private String address;
    private Role role;
    private boolean isActive;
}
