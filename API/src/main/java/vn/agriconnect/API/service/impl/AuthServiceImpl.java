package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.request.auth.LoginRequest;
import vn.agriconnect.API.dto.request.auth.RegisterRequest;
import vn.agriconnect.API.dto.request.auth.TokenRefreshRequest;
import vn.agriconnect.API.dto.response.JwtResponse;
import vn.agriconnect.API.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    public JwtResponse login(LoginRequest request) {
        // TODO: Implement login logic
        return null;
    }

    @Override
    public JwtResponse register(RegisterRequest request) {
        // TODO: Implement registration logic
        return null;
    }

    @Override
    public JwtResponse refreshToken(TokenRefreshRequest request) {
        // TODO: Implement token refresh logic
        return null;
    }

    @Override
    public void logout(String userId) {
        // TODO: Implement logout logic
    }
}
