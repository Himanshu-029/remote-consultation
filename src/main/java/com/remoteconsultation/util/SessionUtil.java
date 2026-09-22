package com.remoteconsultation.util;

import jakarta.servlet.http.HttpSession;

public final class SessionUtil {

    private SessionUtil() {
    }

    public static Long getUserId(HttpSession session) {

        if (session == null) {
            return null;
        }

        Object userId =
                session.getAttribute("userId");

        if (userId instanceof Long) {
            return (Long) userId;
        }

        return null;
    }

    public static String getRole(HttpSession session) {

        if (session == null) {
            return null;
        }

        Object role =
                session.getAttribute("role");

        if (role instanceof String) {
            return (String) role;
        }

        return null;
    }

    public static String getUserName(HttpSession session) {

        if (session == null) {
            return null;
        }

        Object userName =
                session.getAttribute("userName");

        if (userName instanceof String) {
            return (String) userName;
        }

        return null;
    }
}