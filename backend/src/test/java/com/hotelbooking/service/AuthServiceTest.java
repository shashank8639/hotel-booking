package com.hotelbooking.service;

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
import com.hotelbooking.security.UserRole;
import com.hotelbooking.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role customerRole;
    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder().name(UserRole.CUSTOMER).build();
        customerRole.setId(2L);
        user = User.builder()
                .email("rahul@example.com")
                .password("encoded-password")
                .firstName("Rahul")
                .lastName("Sharma")
                .roles(Set.of(customerRole))
                .build();
        user.setId(1L);
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("Rahul@Example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(false);
        when(roleRepository.findByName(UserRole.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(List.of());
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtProperties.getExpirationMs()).thenReturn(3600_000L);
        when(jwtProperties.getRefreshExpirationMs()).thenReturn(604_800_000L);

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600_000L);
        assertThat(response.getUser().getEmail()).isEqualTo("rahul@example.com");
        assertThat(response.getUser().getRoles()).containsExactly("CUSTOMER");

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getToken()).isEqualTo("refresh-token");
        assertThat(tokenCaptor.getValue().isRevoked()).isFalse();
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldReturnTokensWhenCredentialsValid() {
        LoginRequest request = LoginRequest.builder()
                .email("rahul@example.com")
                .password("password123")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        when(userRepository.findByEmailWithRoles("rahul@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(List.of());
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtProperties.getExpirationMs()).thenReturn(3600_000L);
        when(jwtProperties.getRefreshExpirationMs()).thenReturn(604_800_000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getFirstName()).isEqualTo("Rahul");
    }

    @Test
    void login_shouldThrowWhenCredentialsInvalid() {
        LoginRequest request = LoginRequest.builder()
                .email("rahul@example.com")
                .password("wrong-password")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_shouldRotateTokenAndReturnNewTokens() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("old-refresh-token")
                .build();

        RefreshToken storedToken = RefreshToken.builder()
                .token("old-refresh-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        storedToken.setId(10L);

        when(jwtService.isRefreshToken("old-refresh-token")).thenReturn(true);
        when(jwtService.isTokenExpired("old-refresh-token")).thenReturn(false);
        when(jwtService.extractUsername("old-refresh-token")).thenReturn("rahul@example.com");
        when(refreshTokenRepository.findByTokenAndRevokedFalse("old-refresh-token"))
                .thenReturn(Optional.of(storedToken));
        when(userRepository.findByEmailWithRoles("rahul@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(List.of(storedToken));
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtProperties.getExpirationMs()).thenReturn(3600_000L);
        when(jwtProperties.getRefreshExpirationMs()).thenReturn(604_800_000L);

        AuthResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    void refresh_shouldThrowWhenTokenInvalid() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid-token")
                .build();

        when(jwtService.isRefreshToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void logout_shouldRevokeAllActiveTokensForUser() {
        RefreshToken activeToken = RefreshToken.builder()
                .token("refresh-token")
                .user(user)
                .revoked(false)
                .build();
        activeToken.setId(10L);

        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(List.of(activeToken));

        authService.logout(userDetails, null);

        assertThat(activeToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).saveAll(List.of(activeToken));
    }

    @Test
    void logout_shouldRevokeSpecificTokenWhenProvided() {
        RefreshToken activeToken = RefreshToken.builder()
                .token("refresh-token")
                .user(user)
                .revoked(false)
                .build();
        activeToken.setId(10L);
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse("refresh-token"))
                .thenReturn(Optional.of(activeToken));

        authService.logout(userDetails, request);

        assertThat(activeToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(activeToken);
        verify(refreshTokenRepository, never()).findByUserIdAndRevokedFalse(any());
    }

    @Test
    void me_shouldReturnCurrentUserProfile() {
        when(userRepository.findByEmailWithRoles("rahul@example.com")).thenReturn(Optional.of(user));

        AuthUserResponse response = authService.me(userDetails);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("rahul@example.com");
        assertThat(response.getRoles()).containsExactly("CUSTOMER");
    }

    @Test
    void forgotPassword_shouldIssueTokenAndEmailWhenUserExists() {
        when(userRepository.findByEmailAndEnabledTrue("rahul@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.forgotPassword(ForgotPasswordRequest.builder().email("rahul@example.com").build());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).deleteByUserId(1L);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(emailNotificationService).sendPasswordResetEmail(eq("rahul@example.com"), any());
        assertThat(tokenCaptor.getValue().getToken()).isNotBlank();
        assertThat(tokenCaptor.getValue().isUsed()).isFalse();
    }

    /**
     * Mockito practice #2 — anti-enumeration: unknown email must not send mail or persist tokens.
     */
    @Test
    void forgotPassword_shouldNotRevealMissingUser() {
        when(userRepository.findByEmailAndEnabledTrue("missing@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(ForgotPasswordRequest.builder().email("missing@example.com").build());

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailNotificationService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndRevokeSessions() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("reset-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(600))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("reset-token"))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newpassword1")).thenReturn("encoded-new");
        when(userRepository.save(user)).thenReturn(user);
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(List.of());

        authService.resetPassword(ResetPasswordRequest.builder()
                .token("reset-token")
                .newPassword("newpassword1")
                .build());

        assertThat(user.getPassword()).isEqualTo("encoded-new");
        assertThat(resetToken.isUsed()).isTrue();
        verify(passwordResetTokenRepository).save(resetToken);
    }

    @Test
    void resetPassword_shouldRejectUnknownToken() {
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("bad"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(
                ResetPasswordRequest.builder().token("bad").newPassword("newpassword1").build()
        )).isInstanceOf(InvalidPasswordResetTokenException.class);
    }
}
