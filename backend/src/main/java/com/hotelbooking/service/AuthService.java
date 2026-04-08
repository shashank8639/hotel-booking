package com.hotelbooking.service;

import com.hotelbooking.dto.auth.AuthResponse;
import com.hotelbooking.dto.auth.AuthUserResponse;
import com.hotelbooking.dto.auth.ForgotPasswordRequest;
import com.hotelbooking.dto.auth.LoginRequest;
import com.hotelbooking.dto.auth.RefreshTokenRequest;
import com.hotelbooking.dto.auth.RegisterRequest;
import com.hotelbooking.dto.auth.ResetPasswordRequest;
import com.hotelbooking.security.CustomUserDetails;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(CustomUserDetails userDetails, RefreshTokenRequest request);

    AuthUserResponse me(CustomUserDetails userDetails);

    /**
     * Starts password reset. Always succeeds from the caller's perspective (no email enumeration).
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Completes password reset using a one-time token.
     */
    void resetPassword(ResetPasswordRequest request);
}
