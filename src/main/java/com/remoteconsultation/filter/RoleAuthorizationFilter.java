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

public class RoleAuthorizationFilter implements Filter {

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

        /*
         * Public endpoints are handled by AuthenticationFilter.
         * Keep them outside role authorization as well.
         */
        if (path.equals("/login")
                || path.equals("/register")
                || path.equals("/professional-register")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")) {

            chain.doFilter(request, response);
            return;
        }

        HttpSession session =
                httpRequest.getSession(false);

        if (session == null
                || session.getAttribute("role") == null) {

            httpResponse.sendRedirect(
                    contextPath + "/login"
            );

            return;
        }

        String role =
                (String) session.getAttribute("role");

        /*
         * Patient-only URLs
         */
        if (path.startsWith("/patient/")
                && !"PATIENT".equalsIgnoreCase(role)) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to access this resource."
            );

            return;
        }

        /*
         * Professional-only URLs
         */
        if (path.startsWith("/professional/")
                && !"PROFESSIONAL".equalsIgnoreCase(role)) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to access this resource."
            );

            return;
        }

        /*
         * Admin-only URLs
         */
        if ((path.equals("/admin") || path.startsWith("/admin/"))
                && !"ADMIN".equalsIgnoreCase(role)) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to access this resource."
            );

            return;
        }

        chain.doFilter(request, response);
    }
}