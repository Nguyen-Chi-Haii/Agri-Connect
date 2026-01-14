package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.request.auth.LoginRequest;
import vn.agriconnect.API.dto.request.auth.RegisterRequest;
import vn.agriconnect.API.dto.request.auth.TokenRefreshRequest;
import vn.agriconnect.API.dto.response.JwtResponse;
import vn.agriconnect.API.exception.BadRequestException;
import vn.agriconnect.API.exception.ResourceNotFoundException;
import vn.agriconnect.API.model.RefreshToken;
import vn.agriconnect.API.model.User;
import vn.agriconnect.API.model.enums.Role;
import vn.agriconnect.API.repository.RefreshTokenRepository;
import vn.agriconnect.API.repository.UserRepository;
import vn.agriconnect.API.security.jwt.JwtTokenProvider;
import vn.agriconnect.API.service.AuthService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Refresh token expiration: 7 days
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    @Override
    public JwtResponse login(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        return generateTokens(user);
    }

    @Override
    public JwtResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already registered");
        }

        // Check if phone already exists (if provided)
        if (request.getPhone() != null && !request.getPhone().isEmpty()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setAddress(request.getAddress());
        user.setRole(request.getRole() != null ? request.getRole() : Role.FARMER);
        user.setActive(true);

        userRepository.save(user);

        return generateTokens(user);
    }

    @Override
    public JwtResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token has expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Revoke old refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateTokens(user);
    }

    @Override
    public void logout(String userId) {
        // Revoke all refresh tokens for the user
        refreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public String getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
                authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }
        
        return null;
    }

    private JwtResponse generateTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = createRefreshToken(user.getId());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .userId(user.getId())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    private String createRefreshToken(String userId) {
        // Revoke existing refresh tokens for this user
        refreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
                .ifPresent(existingToken -> {
                    existingToken.setRevoked(true);
                    refreshTokenRepository.save(existingToken);
                });

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }
}
