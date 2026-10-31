package com.remoteconsultation.servlet;

import com.remoteconsultation.filter.CsrfProtectionFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        String expectedToken =
                (String) session.getAttribute(
                        CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE);

        String submittedToken =
                request.getParameter("_csrf");

        if (expectedToken == null
                || submittedToken == null
                || submittedToken.isBlank()
                || !MessageDigest.isEqual(
                        expectedToken.getBytes(StandardCharsets.UTF_8),
                        submittedToken.getBytes(StandardCharsets.UTF_8))) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid or missing CSRF token.");
            return;
        }

        session.invalidate();

        response.sendRedirect(
                request.getContextPath() + "/login");
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Allow", "POST");
        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Logout requires POST.");
    }
}