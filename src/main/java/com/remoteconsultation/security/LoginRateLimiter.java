
package com.remoteconsultation.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginRateLimiter {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS =
            Duration.ofMinutes(15).toMillis();

    private final Map<String, AttemptWindow> attempts =
            new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        if (key == null || key.isBlank()) {
            return true;
        }

        long now = System.currentTimeMillis();

        AttemptWindow window = attempts.get(key);

        if (window == null) {
            return false;
        }

        synchronized (window) {
            if (now - window.windowStartedAt >= WINDOW_MILLIS) {
                attempts.remove(key, window);
                return false;
            }

            return window.failedAttempts >= MAX_FAILED_ATTEMPTS;
        }
    }

    public void recordFailure(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        long now = System.currentTimeMillis();

        AttemptWindow window = attempts.computeIfAbsent(
                key,
                ignored -> new AttemptWindow(now)
        );

        synchronized (window) {
            if (now - window.windowStartedAt >= WINDOW_MILLIS) {
                window.windowStartedAt = now;
                window.failedAttempts = 0;
            }

            window.failedAttempts++;
        }
    }

    public void recordSuccess(String key) {
        if (key != null && !key.isBlank()) {
            attempts.remove(key);
        }
    }

    private static final class AttemptWindow {

        private long windowStartedAt;
        private int failedAttempts;

        private AttemptWindow(long startedAt) {
            this.windowStartedAt = startedAt;
            this.failedAttempts = 0;
        }
    }
}
