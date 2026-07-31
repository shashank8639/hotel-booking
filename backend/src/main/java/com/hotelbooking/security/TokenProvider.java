package com.hotelbooking.security;

import com.hotelbooking.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Contract for JWT access/refresh token creation and validation.
 */
public interface TokenProvider {

    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isAccessToken(String token);

    boolean isRefreshToken(String token);
}
