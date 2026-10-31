
package com.remoteconsultation.util;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9-]+(?:\\.[A-Z0-9-]+)+$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
    );

    private InputValidator() {
        // Utility class; do not instantiate.
    }

    public static boolean isValidEmail(String email) {
        return email != null
                && email.length() <= 254
                && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
    if (password == null || password.length() < 8) {
        return false;
    }

    return password.getBytes(
            java.nio.charset.StandardCharsets.UTF_8
    ).length <= 72;

}
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }

        String normalized = phone.trim();

        return PHONE_PATTERN.matcher(normalized).matches();
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
