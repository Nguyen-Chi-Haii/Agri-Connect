package vn.agriconnect.API.security.jwt;

import org.springframework.stereotype.Component;

/**
 * JWT Token Provider
 * - Generate access tokens
 * - Generate refresh tokens
 * - Validate tokens
 * - Extract claims from tokens
 */
@Component
public class JwtTokenProvider {

    // TODO: Implement token generation
    public String generateAccessToken(String userId) {
        return null;
    }

    // TODO: Implement token validation
    public boolean validateToken(String token) {
        return false;
    }

    // TODO: Implement claims extraction
    public String getUserIdFromToken(String token) {
        return null;
    }
}
