package vn.agriconnect.API.mapper;

import org.springframework.stereotype.Component;
import vn.agriconnect.API.dto.response.UserProfileResponse;
import vn.agriconnect.API.model.User;

@Component
public class UserMapper {

    public UserProfileResponse toResponse(User user) {
        if (user == null) return null;
        
        return UserProfileResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .address(user.getAddress())
                .role(user.getRole())
                .isActive(user.isActive())
                .build();
    }
}
