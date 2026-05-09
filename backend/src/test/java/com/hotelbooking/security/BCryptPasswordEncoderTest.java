package com.hotelbooking.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates BCrypt salting: same password → different hashes; matches() still works.
 */
class BCryptPasswordEncoderTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void encode_samePasswordTwice_producesDifferentHashes_butBothMatch() {
        String raw = "password123";

        String hash1 = encoder.encode(raw);
        String hash2 = encoder.encode(raw);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(encoder.matches(raw, hash1)).isTrue();
        assertThat(encoder.matches(raw, hash2)).isTrue();
        assertThat(encoder.matches("wrong-password", hash1)).isFalse();
    }
}
