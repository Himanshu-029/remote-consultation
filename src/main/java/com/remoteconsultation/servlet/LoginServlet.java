
package com.remoteconsultation.servlet;

import com.remoteconsultation.dto.LoginRequest;
import com.remoteconsultation.filter.CsrfProtectionFilter;
import com.remoteconsultation.model.User;
import com.remoteconsultation.security.LoginRateLimiter;
import com.remoteconsultation.service.UserService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

public class LoginServlet extends HttpServlet {

    private final UserService userService;
    private final LoginRateLimiter loginRateLimiter;

    public LoginServlet(
            UserService userService,
            LoginRateLimiter loginRateLimiter) {

        this.userService = userService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);

        request.setAttribute(
                "csrfToken",
                CsrfProtectionFilter.ensureToken(session)
        );

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(
                        "/WEB-INF/jsp/login.jsp"
                );

        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        String email = request.getParameter("email");
        String normalizedEmail =
                email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        String ipKey = "ip:" + clientIp;
        String accountKey =
                "account:" + hashKey(clientIp + "|" + normalizedEmail);

        // Block requests that exceed either limit.
        if (loginRateLimiter.isBlocked(ipKey)
                || loginRateLimiter.isBlocked(accountKey)) {

            response.setHeader("Retry-After", "900");
            response.sendError(
                429,
                "Too many login attempts. Please try again later."
                );
            return;
        }

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(request.getParameter("password"));

        try {
            User user = userService.login(loginRequest);

            // Record success for the account-specific key only.
            // Keep the IP-wide counter to protect other accounts.
            loginRateLimiter.recordSuccess(accountKey);

            /*
             * Invalidate the pre-login session to prevent
             * session fixation.
             */
            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Create a fresh authenticated session.
            HttpSession session = request.getSession(true);

            session.setAttribute("userId", user.getUserId());
            session.setAttribute("role", user.getRole());
            session.setAttribute("userName", user.getFullName());

            // Generate a fresh CSRF token for the new session.
            CsrfProtectionFilter.ensureToken(session);

            response.sendRedirect(
                    request.getContextPath() + "/dashboard"
            );

        } catch (IllegalArgumentException e) {
            loginRateLimiter.recordFailure(ipKey);
            loginRateLimiter.recordFailure(accountKey);

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid email or password."
            );

        } catch (Exception e) {
            throw new ServletException("Login failed.", e);
        }
    }

    private String hashKey(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable.", e
            );
        }
    }
}
