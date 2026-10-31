
package com.remoteconsultation.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class CsrfProtectionFilter implements Filter {

    public static final String SESSION_TOKEN_ATTRIBUTE =
            "csrfToken";

    public static final String REQUEST_TOKEN_PARAMETER =
            "_csrf";

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final String CSRF_ERROR_MESSAGE =
            "CSRF validation failed. Please refresh the page and try again.";

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        String method = httpRequest.getMethod();

        /*
         * Allow safe HTTP methods without CSRF validation.
         * TRACE is intentionally excluded.
         */
        if ("GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)) {

            String requestUri = httpRequest.getRequestURI();
            String contextPath = httpRequest.getContextPath();

            if (requestUri == null) {
                requestUri = "/";
            }

            if (contextPath == null) {
                contextPath = "";
            }

            String path = requestUri.startsWith(contextPath)
                    ? requestUri.substring(contextPath.length())
                    : requestUri;

            boolean isStaticResource =
                    path.startsWith("/css/")
                    || path.startsWith("/js/")
                    || path.startsWith("/images/")
                    || path.equals("/favicon.ico");

            if (!isStaticResource) {
                ensureToken(httpRequest.getSession(true));
            }

            chain.doFilter(request, response);
            return;
        }

        /*
         * All other HTTP methods require a valid CSRF token.
         * Do not create a new session for a state-changing request.
         */
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    CSRF_ERROR_MESSAGE);
            return;
        }

        String expectedToken =
                (String) session.getAttribute(
                        SESSION_TOKEN_ATTRIBUTE);

        String submittedToken =
                httpRequest.getParameter(
                        REQUEST_TOKEN_PARAMETER);

        if (expectedToken == null
                || submittedToken == null
                || submittedToken.isBlank()
                || !MessageDigest.isEqual(
                        expectedToken.getBytes(StandardCharsets.UTF_8),
                        submittedToken.getBytes(StandardCharsets.UTF_8))) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    CSRF_ERROR_MESSAGE);
            return;
        }

        chain.doFilter(request, response);
    }

    public static String ensureToken(HttpSession session) {

        Object existing =
                session.getAttribute(
                        SESSION_TOKEN_ATTRIBUTE);

        if (existing instanceof String token
                && !token.isBlank()) {
            return token;
        }

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        session.setAttribute(
                SESSION_TOKEN_ATTRIBUTE,
                token);

        return token;
    }
}
