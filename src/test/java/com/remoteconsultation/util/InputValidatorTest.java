
package com.remoteconsultation.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    @Test
    void acceptsValidEmail() {
        assertTrue(InputValidator.isValidEmail("patient@example.com"));
    }

    @Test
    void rejectsInvalidEmail() {
        assertFalse(InputValidator.isValidEmail(null));
        assertFalse(InputValidator.isValidEmail(""));
        assertFalse(InputValidator.isValidEmail("not-an-email"));
        assertFalse(InputValidator.isValidEmail("patient@"));
    }

    @Test
    void acceptsPasswordWithinLimits() {
        assertTrue(InputValidator.isValidPassword("Secure123"));
        assertTrue(InputValidator.isValidPassword("a".repeat(72)));
    }

    @Test
    void rejectsPasswordOutsideLimits() {
        assertFalse(InputValidator.isValidPassword(null));
        assertFalse(InputValidator.isValidPassword(""));
        assertFalse(InputValidator.isValidPassword("short"));

        // 73 ASCII bytes exceed BCrypt's 72-byte limit.
        assertFalse(InputValidator.isValidPassword("a".repeat(73)));
    }

    @Test
    void rejectsPasswordExceedingUtf8ByteLimit() {
        // Each emoji occupies multiple UTF-8 bytes.
        String password = "a".repeat(8) + "😀".repeat(17);

        assertTrue(
                password.getBytes(StandardCharsets.UTF_8).length > 72
        );
        assertFalse(InputValidator.isValidPassword(password));
    }

    @Test
    void acceptsValidPhoneNumber() {
        assertTrue(InputValidator.isValidPhone("9876543210"));
        assertTrue(InputValidator.isValidPhone("+919876543210"));
    }

    @Test
    void rejectsInvalidPhoneNumber() {
        assertFalse(InputValidator.isValidPhone(null));
        assertFalse(InputValidator.isValidPhone(""));
        assertFalse(InputValidator.isValidPhone("12345"));
        assertFalse(InputValidator.isValidPhone("abcdefghij"));
        assertFalse(InputValidator.isValidPhone("1234567890123456"));
    }

    @Test
    void checksRequiredText() {
        assertTrue(InputValidator.isNotBlank("Patient"));
        assertFalse(InputValidator.isNotBlank(null));
        assertFalse(InputValidator.isNotBlank(""));
        assertFalse(InputValidator.isNotBlank("   "));
    }
}
