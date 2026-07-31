package com.hotelbooking.security;

import com.hotelbooking.config.JwtProperties;
import com.hotelbooking.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates and validates JWT access and refresh tokens using HS256.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService implements TokenProvider {

    private final JwtProperties jwtProperties;

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        return buildToken(
                userDetails.getUsername(),
                roles,
                SecurityConstants.ACCESS_TOKEN_TYPE,
                jwtProperties.getExpirationMs()
        );
    }

    @Override
    public String generateRefreshToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> SecurityConstants.ROLE_PREFIX + role.getName().name())
                .collect(Collectors.toList());

        return buildToken(
                user.getEmail(),
                roles,
                SecurityConstants.REFRESH_TOKEN_TYPE,
                jwtProperties.getRefreshExpirationMs()
        );
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean isAccessToken(String token) {
        return SecurityConstants.ACCESS_TOKEN_TYPE.equals(extractTokenType(token));
    }

    @Override
    public boolean isRefreshToken(String token) {
        return SecurityConstants.REFRESH_TOKEN_TYPE.equals(extractTokenType(token));
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private String extractTokenType(String token) {
        return extractAllClaims(token).get(SecurityConstants.TOKEN_TYPE_CLAIM, String.class);
    }

    private String buildToken(String subject, List<String> roles, String tokenType, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(subject)
                .claim(SecurityConstants.ROLES_CLAIM, roles)
                .claim(SecurityConstants.TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        String secret = jwtProperties.getSecret();
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
