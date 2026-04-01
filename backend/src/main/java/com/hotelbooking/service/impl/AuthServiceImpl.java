package com.hotelbooking.service.impl;

import com.hotelbooking.config.JwtProperties;
import com.hotelbooking.dto.auth.AuthResponse;
import com.hotelbooking.dto.auth.AuthUserResponse;
import com.hotelbooking.dto.auth.ForgotPasswordRequest;
import com.hotelbooking.dto.auth.LoginRequest;
import com.hotelbooking.dto.auth.RefreshTokenRequest;
import com.hotelbooking.dto.auth.RegisterRequest;
import com.hotelbooking.dto.auth.ResetPasswordRequest;
import com.hotelbooking.entity.PasswordResetToken;
import com.hotelbooking.entity.RefreshToken;
import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.User;
import com.hotelbooking.exception.DuplicateEmailException;
import com.hotelbooking.exception.InvalidCredentialsException;
import com.hotelbooking.exception.InvalidPasswordResetTokenException;
import com.hotelbooking.exception.InvalidRefreshTokenException;
import com.hotelbooking.repository.PasswordResetTokenRepository;
import com.hotelbooking.repository.RefreshTokenRepository;
import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.CustomUserDetails;
import com.hotelbooking.security.EmailNotificationService;
import com.hotelbooking.security.JwtService;
import com.hotelbooking.security.SecurityConstants;
import com.hotelbooking.security.TokenUtils;
import com.hotelbooking.security.UserRole;
import com.hotelbooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailNotificationService emailNotificationService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("Registering user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        Role customerRole = roleRepository.findByName(UserRole.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role is not seeded"));

        User user = User.builder()
                .email(email)
                .password(TokenUtils.encode(passwordEncoder, request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .roles(Set.of(customerRole))
                .build();

        User savedUser = userRepository.save(user);
        log.debug("User registered with id: {}", savedUser.getId());
        return issueAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("Login attempt for email: {}", email);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (AuthenticationException ex) {
            log.debug("Authentication failed for email {}: {}", email, ex.getMessage());
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(InvalidCredentialsException::new);

        return issueAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenValue = request.getRefreshToken();
        log.debug("Refreshing access token");

        if (!jwtService.isRefreshToken(tokenValue) || jwtService.isTokenExpired(tokenValue)) {
            throw new InvalidRefreshTokenException();
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(tokenValue)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new InvalidRefreshTokenException();
        }

        String email = jwtService.extractUsername(tokenValue);
        if (!email.equals(storedToken.getUser().getEmail())) {
            throw new InvalidRefreshTokenException();
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(InvalidRefreshTokenException::new);

        return issueAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(CustomUserDetails userDetails, RefreshTokenRequest request) {
        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            log.info("Logging out user id: {} via refresh token", userDetails.getUser().getId());
            refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                    .filter(token -> token.getUser().getId().equals(userDetails.getUser().getId()))
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
            return;
        }

        log.info("Logging out user id: {}", userDetails.getUser().getId());
        revokeActiveRefreshTokens(userDetails.getUser().getId());
    }

    @Override
    public AuthUserResponse me(CustomUserDetails userDetails) {
        String email = userDetails.getUsername();
        log.debug("Fetching profile for email: {}", email);

        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(InvalidCredentialsException::new);

        return toAuthUserResponse(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("Password reset requested for email: {}", email);

        // Always return quietly — do not reveal whether the email exists.
        userRepository.findByEmailAndEnabledTrue(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String token = TokenUtils.generateSecureToken(32);
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(Instant.now().plus(SecurityConstants.PASSWORD_RESET_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            emailNotificationService.sendPasswordResetEmail(user.getEmail(), token);
            log.debug("Password reset token issued for userId={}", user.getId());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset completion attempted");

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            throw new InvalidPasswordResetTokenException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(TokenUtils.encode(passwordEncoder, request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        revokeActiveRefreshTokens(user.getId());
        log.info("Password reset completed for userId={}", user.getId());
    }

    private AuthResponse issueAuthResponse(User user) {
        revokeActiveRefreshTokens(user.getId());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(user);

        persistRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpirationMs())
                .user(toAuthUserResponse(user))
                .build();
    }

    private void persistRefreshToken(User user, String refreshToken) {
        RefreshToken entity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);
    }

    private void revokeActiveRefreshTokens(Long userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
        }
        if (!activeTokens.isEmpty()) {
            refreshTokenRepository.saveAll(activeTokens);
        }
    }

    private AuthUserResponse toAuthUserResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();

        return AuthUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
