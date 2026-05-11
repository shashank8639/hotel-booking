package com.hotelbooking.security;

import com.hotelbooking.config.JwtProperties;
import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JWT create/validate without Spring context.
 * <p>
 * Verifies: access vs refresh token types, subject extraction, expiry, invalid tokens.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private CustomUserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        // Must be valid Base64 (≥256-bit key). JwtService BASE64-decodes first.
        properties.setSecret("MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=");
        properties.setExpirationMs(3_600_000L);
        properties.setRefreshExpirationMs(86_400_000L);
        jwtService = new JwtService(properties);

        user = User.builder()
                .email("rahul@example.com")
                .password("secret")
                .firstName("Rahul")
                .lastName("Sharma")
                .roles(Set.of(Role.builder().name(UserRole.CUSTOMER).build()))
                .build();
        user.setId(1L);
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void generateAccessToken_shouldBeValidAccessToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(jwtService.isAccessToken(token)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isFalse();
        assertThat(jwtService.extractUsername(token)).isEqualTo("rahul@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void generateRefreshToken_shouldBeValidRefreshToken() {
        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.isRefreshToken(token)).isTrue();
        assertThat(jwtService.isAccessToken(token)).isFalse();
        assertThat(jwtService.extractUsername(token)).isEqualTo("rahul@example.com");
    }

    @Test
    void isTokenValid_shouldReturnFalseForWrongUser() {
        String token = jwtService.generateAccessToken(userDetails);
        User other = User.builder()
                .email("other@example.com")
                .password("x")
                .firstName("O")
                .lastName("T")
                .roles(Set.of(Role.builder().name(UserRole.CUSTOMER).build()))
                .build();

        assertThat(jwtService.isTokenValid(token, new CustomUserDetails(other))).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseForGarbageToken() {
        assertThat(jwtService.isTokenValid("not.a.jwt", userDetails)).isFalse();
    }

    @Test
    void expiredToken_shouldNotBeValid() {
        JwtProperties shortLived = new JwtProperties();
        shortLived.setSecret("MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=");
        shortLived.setExpirationMs(1L);
        JwtService shortService = new JwtService(shortLived);

        String token = shortService.generateAccessToken(userDetails);
        try {
            Thread.sleep(5L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(shortService.isTokenExpired(token)).isTrue();
        assertThat(shortService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void extractRoles_readsRolesClaimFromAccessToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void extractTokenType_distinguishesAccessAndRefresh() {
        String access = jwtService.generateAccessToken(userDetails);
        String refresh = jwtService.generateRefreshToken(user);

        assertThat(jwtService.extractTokenType(access)).isEqualTo(SecurityConstants.ACCESS_TOKEN_TYPE);
        assertThat(jwtService.extractTokenType(refresh)).isEqualTo(SecurityConstants.REFRESH_TOKEN_TYPE);
        assertThat(jwtService.isAccessToken(access)).isTrue();
        assertThat(jwtService.isRefreshToken(refresh)).isTrue();
        assertThat(jwtService.isAccessToken(refresh)).isFalse();
    }
}
