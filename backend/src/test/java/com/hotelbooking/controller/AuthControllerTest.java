package com.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.dto.auth.AuthResponse;
import com.hotelbooking.dto.auth.AuthUserResponse;
import com.hotelbooking.dto.auth.ForgotPasswordRequest;
import com.hotelbooking.dto.auth.LoginRequest;
import com.hotelbooking.dto.auth.RefreshTokenRequest;
import com.hotelbooking.dto.auth.RegisterRequest;
import com.hotelbooking.dto.auth.ResetPasswordRequest;
import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.User;
import com.hotelbooking.exception.DuplicateEmailException;
import com.hotelbooking.exception.GlobalExceptionHandler;
import com.hotelbooking.exception.InvalidCredentialsException;
import com.hotelbooking.exception.InvalidRefreshTokenException;
import com.hotelbooking.security.CustomUserDetails;
import com.hotelbooking.security.UserRole;
import com.hotelbooking.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User user = User.builder()
                .email("rahul@example.com")
                .firstName("Rahul")
                .lastName("Sharma")
                .roles(Set.of(Role.builder().name(UserRole.CUSTOMER).build()))
                .build();
        user.setId(1L);
        userDetails = new CustomUserDetails(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldReturn201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul@example.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600_000L)
                .user(AuthUserResponse.builder()
                        .id(1L)
                        .email("rahul@example.com")
                        .firstName("Rahul")
                        .lastName("Sharma")
                        .roles(List.of("CUSTOMER"))
                        .build())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600_000))
                .andExpect(jsonPath("$.user.email").value("rahul@example.com"));
    }

    @Test
    void register_shouldReturn400WhenValidationFails() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("")
                .lastName("Sharma")
                .email("invalid-email")
                .password("short")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void register_shouldReturn409WhenDuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul@example.com")
                .password("password123")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateEmailException("rahul@example.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturn200() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("rahul@example.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600_000L)
                .user(AuthUserResponse.builder().id(1L).email("rahul@example.com").build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void login_shouldReturn401WhenCredentialsInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("rahul@example.com")
                .password("wrong-password")
                .build();

        when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturn200() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .expiresIn(3600_000L)
                .user(AuthUserResponse.builder().id(1L).email("rahul@example.com").build())
                .build();

        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void refresh_shouldReturn401WhenTokenInvalid() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid-token")
                .build();

        when(authService.refresh(any(RefreshTokenRequest.class)))
                .thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturn204() throws Exception {
        authenticate(userDetails);
        doNothing().when(authService).logout(eq(userDetails), isNull());

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent());

        verify(authService).logout(eq(userDetails), isNull());
    }

    @Test
    void me_shouldReturn200() throws Exception {
        authenticate(userDetails);

        AuthUserResponse response = AuthUserResponse.builder()
                .id(1L)
                .email("rahul@example.com")
                .firstName("Rahul")
                .lastName("Sharma")
                .roles(List.of("CUSTOMER"))
                .build();

        when(authService.me(userDetails)).thenReturn(response);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("rahul@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
    }

    @Test
    void forgotPassword_shouldReturn204() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("rahul@example.com")
                .build();
        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void resetPassword_shouldReturn204() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("reset-token")
                .newPassword("newpassword1")
                .build();
        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }

    private void authenticate(CustomUserDetails details) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
