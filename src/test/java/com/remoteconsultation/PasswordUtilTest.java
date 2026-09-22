package com.remoteconsultation;

import com.remoteconsultation.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void passwordHashingShouldWork() {

        PasswordUtil passwordUtil = new PasswordUtil();

        String rawPassword = "Test@12345";

        String hashedPassword =
                passwordUtil.hashPassword(rawPassword);

        assertNotNull(hashedPassword);
        assertNotEquals(rawPassword, hashedPassword);

        assertTrue(
                passwordUtil.verifyPassword(
                        rawPassword,
                        hashedPassword
                )
        );

        assertFalse(
                passwordUtil.verifyPassword(
                        "Wrong@123",
                        hashedPassword
                )
        );
    }
}