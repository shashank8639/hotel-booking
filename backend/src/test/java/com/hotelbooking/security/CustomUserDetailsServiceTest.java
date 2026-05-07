package com.hotelbooking.security;

import com.hotelbooking.entity.User;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_shouldReturnDetailsForEnabledUser() {
        User user = TestDataFactory.customerUser("rahul@example.com");
        user.setId(10L);
        when(userRepository.findByEmailWithRoles("rahul@example.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("rahul@example.com");

        assertThat(details.getUsername()).isEqualTo("rahul@example.com");
        assertThat(details.getAuthorities()).extracting(Object::toString)
                .contains("ROLE_CUSTOMER");
    }

    @Test
    void loadUserByUsername_shouldRejectDisabledUser() {
        User user = TestDataFactory.customerUser("disabled@example.com");
        user.setEnabled(false);
        when(userRepository.findByEmailWithRoles("disabled@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.loadUserByUsername("disabled@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_shouldThrowWhenMissing() {
        when(userRepository.findByEmailWithRoles("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@example.com");
    }
}
