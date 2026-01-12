package vn.agriconnect.API.service;

import vn.agriconnect.API.dto.request.user.KycSubmissionRequest;
import vn.agriconnect.API.dto.request.user.UpdateProfileRequest;
import vn.agriconnect.API.dto.response.UserProfileResponse;
import vn.agriconnect.API.model.User;

import java.util.List;

public interface UserService {
    List<UserProfileResponse> getAllUsers();

    UserProfileResponse getProfile(String userId);

    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);

    User findById(String userId);

    List<User> findAll();

    void deleteUser(String userId);

    // KYC methods
    UserProfileResponse submitKyc(String userId, KycSubmissionRequest request);

    void verifyKyc(String userId);

    void rejectKyc(String userId, String reason);
}
