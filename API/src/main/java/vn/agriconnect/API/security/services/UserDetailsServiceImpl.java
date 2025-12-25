package vn.agriconnect.API.security.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService Implementation
 * - Loads user by username/email for authentication
 * - Used by Spring Security
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Load user from database
        // TODO: Convert to UserDetails
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
