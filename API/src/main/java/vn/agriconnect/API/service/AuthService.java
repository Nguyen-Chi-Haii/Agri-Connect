package vn.agriconnect.API.service;

import vn.agriconnect.API.dto.request.auth.LoginRequest;
import vn.agriconnect.API.dto.request.auth.RegisterRequest;
import vn.agriconnect.API.dto.request.auth.TokenRefreshRequest;
import vn.agriconnect.API.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    JwtResponse register(RegisterRequest request);
    JwtResponse refreshToken(TokenRefreshRequest request);
    void logout(String userId);
}
