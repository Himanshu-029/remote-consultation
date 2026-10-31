
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
import java.util.Locale;
import java.util.Set;

public class RoleAuthorizationFilter implements Filter {

    private static final Set<String> VALID_ROLES = Set.of(
            "PATIENT",
            "PROFESSIONAL",
            "ADMIN"
    );

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

        String requestUri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        String path = requestUri.substring(
                contextPath.length()
        );

        // Public endpoints and static resources.
        if (path.equals("/login")
                || path.equals("/register")
                || path.equals("/professional-register")
                || path.equals("/favicon.ico")
                || path.equals("/error")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")) {

            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null
                || session.getAttribute("role") == null) {

            httpResponse.sendRedirect(
                    contextPath + "/login"
            );
            return;
        }

        Object roleAttribute = session.getAttribute("role");

        if (!(roleAttribute instanceof String)) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid user role."
            );
            return;
        }

        String role = ((String) roleAttribute)
                .trim()
                .toUpperCase(Locale.ROOT);

        // Reject unexpected roles instead of allowing access by default.
        if (!VALID_ROLES.contains(role)) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid user role."
            );
            return;
        }

        // Patient-only endpoints.
        if (path.startsWith("/patient/")
                && !"PATIENT".equals(role)) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to access this resource."
            );
            return;
        }

        // Professional-only endpoints.
        if (path.startsWith("/professional/")
                && !"PROFESSIONAL".equals(role)) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to access this resource."
            );
            return;
        }

        // Admin-only endpoints.
        if ((path.equals("/admin")
                || path.startsWith("/admin/"))
                && !"ADMIN".equals(role)) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to access this resource."
            );
            return;
        }

        chain.doFilter(request, response);
    }
}
