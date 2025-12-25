package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.request.user.UpdateProfileRequest;
import vn.agriconnect.API.dto.response.UserProfileResponse;
import vn.agriconnect.API.model.User;
import vn.agriconnect.API.repository.UserRepository;
import vn.agriconnect.API.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getProfile(String userId) {
        // TODO: Implement
        return null;
    }

    @Override
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        // TODO: Implement
        return null;
    }

    @Override
    public User findById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}
