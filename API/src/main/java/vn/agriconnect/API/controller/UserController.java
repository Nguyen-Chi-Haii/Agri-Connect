package vn.agriconnect.API.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.request.user.KycSubmissionRequest;
import vn.agriconnect.API.dto.request.user.UpdateProfileRequest;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.dto.response.UserProfileResponse;
import vn.agriconnect.API.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<UserProfileResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String kycStatus
    ) {
        java.util.List<UserProfileResponse> users = userService.getAllUsers(search, role, kycStatus);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfileResponse profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable String userId) {
        UserProfileResponse profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfileResponse profile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", profile));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    // ==================== KYC Endpoints ====================

    @PostMapping("/kyc/submit")
    public ResponseEntity<ApiResponse<UserProfileResponse>> submitKyc(
            @Valid @RequestBody KycSubmissionRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfileResponse profile = userService.submitKyc(userId, request);
        return ResponseEntity.ok(ApiResponse.success("KYC submitted successfully", profile));
    }

    @PutMapping("/{userId}/kyc/verify")
    public ResponseEntity<ApiResponse<Void>> verifyKyc(@PathVariable String userId) {
        userService.verifyKyc(userId);
        return ResponseEntity.ok(ApiResponse.success("KYC verified successfully", null));
    }

    @PutMapping("/{userId}/kyc/reject")
    public ResponseEntity<ApiResponse<Void>> rejectKyc(
            @PathVariable String userId,
            @RequestParam(required = false) String reason) {
        userService.rejectKyc(userId, reason);
        return ResponseEntity.ok(ApiResponse.success("KYC rejected", null));
    }

    @PutMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable String userId) {
        userService.lockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Account locked", null));
    }

    @PutMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable String userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unlocked", null));
    }
}
