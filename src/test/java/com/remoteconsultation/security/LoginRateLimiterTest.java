
package com.remoteconsultation.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginRateLimiter();
    }

    @Test
    void shouldNotBlockBeforeFiveFailedAttempts() {
        String clientIp = "192.168.1.10";

        for (int i = 0; i < 4; i++) {
            limiter.recordFailure(clientIp);
        }

        assertFalse(limiter.isBlocked(clientIp));
    }

    @Test
    void shouldBlockAfterFiveFailedAttempts() {
        String clientIp = "192.168.1.11";

        for (int i = 0; i < 5; i++) {
            limiter.recordFailure(clientIp);
        }

        assertTrue(limiter.isBlocked(clientIp));
    }

    @Test
    void shouldResetAttemptsAfterSuccessfulLogin() {
        String clientIp = "192.168.1.12";

        for (int i = 0; i < 5; i++) {
            limiter.recordFailure(clientIp);
        }

        assertTrue(limiter.isBlocked(clientIp));

        limiter.recordSuccess(clientIp);

        assertFalse(limiter.isBlocked(clientIp));
    }

    @Test
    void shouldTrackDifferentClientsSeparately() {
        String firstClient = "192.168.1.20";
        String secondClient = "192.168.1.21";

        for (int i = 0; i < 5; i++) {
            limiter.recordFailure(firstClient);
        }

        assertTrue(limiter.isBlocked(firstClient));
        assertFalse(limiter.isBlocked(secondClient));
    }
}
