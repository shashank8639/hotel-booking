package com.hotelbooking.repository;

import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.User;
import com.hotelbooking.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        Role customer = roleRepository.save(Role.builder().name(UserRole.CUSTOMER).build());
        User user = User.builder()
                .email("repo.user@example.com")
                .password("encoded")
                .firstName("Repo")
                .lastName("User")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of(customer))
                .build();
        userRepository.save(user);
    }

    @Test
    void findByEmailWithRoles_eagerLoadsRoles() {
        var found = userRepository.findByEmailWithRoles("repo.user@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).extracting(Role::getName).contains(UserRole.CUSTOMER);
    }

    @Test
    void existsByEmail_supportsRegistrationGuard() {
        assertThat(userRepository.existsByEmail("repo.user@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }
}
