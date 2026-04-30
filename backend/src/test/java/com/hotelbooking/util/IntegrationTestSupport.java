package com.hotelbooking.util;

import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.User;
import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.CustomUserDetails;
import com.hotelbooking.security.JwtService;
import com.hotelbooking.security.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Shared helpers for {@code @SpringBootTest} integration suites.
 */
public final class IntegrationTestSupport {

    private IntegrationTestSupport() {
    }

    public static void ensureRoles(RoleRepository roleRepository) {
        if (!roleRepository.existsByName(UserRole.CUSTOMER)) {
            roleRepository.save(Role.builder().name(UserRole.CUSTOMER).build());
        }
        if (!roleRepository.existsByName(UserRole.ADMIN)) {
            roleRepository.save(Role.builder().name(UserRole.ADMIN).build());
        }
    }

    public static User persistUser(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String rawPassword,
            UserRole roleName
    ) {
        ensureRoles(roleRepository);
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .firstName("Test")
                .lastName(roleName.name())
                .enabled(true)
                .accountNonLocked(true)
                .roles(java.util.Set.of(role))
                .build();
        return userRepository.save(user);
    }

    public static String bearerAccessToken(JwtService jwtService, User user) {
        String token = jwtService.generateAccessToken(new CustomUserDetails(user));
        return "Bearer " + token;
    }
}
