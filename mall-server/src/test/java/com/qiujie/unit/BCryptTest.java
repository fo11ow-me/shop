package com.qiujie.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BCrypt password encoding tests")
class BCryptTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("encode then matches — same password returns true")
    void shouldMatchAfterEncode() {
        String rawPassword = "admin123";
        String encoded = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encoded);
        assertTrue(encoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("matches — wrong password returns false")
    void shouldNotMatchWrongPassword() {
        String encoded = encoder.encode("correct_password");
        assertFalse(encoder.matches("wrong_password", encoded));
    }

    @Test
    @DisplayName("encode — same input produces different hashes (salt)")
    void shouldProduceDifferentHashes() {
        String raw = "same_input";
        String hash1 = encoder.encode(raw);
        String hash2 = encoder.encode(raw);

        assertNotEquals(hash1, hash2);
        assertTrue(encoder.matches(raw, hash1));
        assertTrue(encoder.matches(raw, hash2));
    }
}
