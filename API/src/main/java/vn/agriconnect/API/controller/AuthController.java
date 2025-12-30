package vn.agriconnect.API.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.request.auth.LoginRequest;
import vn.agriconnect.API.dto.request.auth.RegisterRequest;
import vn.agriconnect.API.dto.request.auth.SendOtpRequest;
import vn.agriconnect.API.dto.request.auth.TokenRefreshRequest;
import vn.agriconnect.API.dto.request.auth.VerifyOtpRequest;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.dto.response.JwtResponse;
import vn.agriconnect.API.dto.response.OtpResponse;
import vn.agriconnect.API.service.AuthService;
import vn.agriconnect.API.service.OtpService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JwtResponse>> register(@Valid @RequestBody RegisterRequest request) {
        JwtResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        JwtResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    // ==================== OTP Endpoints ====================

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = otpService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", response));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpResponse response = otpService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Phone verified successfully", response));
    }
}
