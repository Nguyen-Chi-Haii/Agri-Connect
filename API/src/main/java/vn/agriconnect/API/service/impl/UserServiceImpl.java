package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.agriconnect.API.dto.request.user.KycSubmissionRequest;
import vn.agriconnect.API.dto.request.user.UpdateProfileRequest;
import vn.agriconnect.API.dto.response.UserProfileResponse;
import vn.agriconnect.API.exception.ResourceNotFoundException;
import vn.agriconnect.API.model.User;
import vn.agriconnect.API.model.embedded.KycInfo;
import vn.agriconnect.API.repository.UserRepository;
import vn.agriconnect.API.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (StringUtils.hasText(request.getAddress())) {
            user.setAddress(request.getAddress());
        }

        userRepository.save(user);
        return mapToResponse(user);
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

    // ==================== KYC Methods ====================

    @Override
    public UserProfileResponse submitKyc(String userId, KycSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        KycInfo kyc = new KycInfo();
        kyc.setCccd(request.getIdNumber());
        kyc.setCccdFrontImage(request.getIdFrontImage());
        kyc.setCccdBackImage(request.getIdBackImage());
        kyc.setStatus("PENDING");
        kyc.setRejectionReason(null);

        user.setKyc(kyc);
        userRepository.save(user);

        return mapToResponse(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void verifyKyc(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getKyc() == null) {
            throw new ResourceNotFoundException("KYC info not found for user", "id", userId);
        }

        user.getKyc().setStatus("VERIFIED");
        user.getKyc().setRejectionReason(null);
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void rejectKyc(String userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getKyc() == null) {
            throw new ResourceNotFoundException("KYC info not found for user", "id", userId);
        }

        user.getKyc().setStatus("REJECTED");
        user.getKyc().setRejectionReason(reason);
        userRepository.save(user);
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .address(user.getAddress())
                .role(user.getRole())
                .isActive(user.isActive())
                .kyc(user.getKyc())
                .build();
    }
}
