
package com.remoteconsultation.servlet;

import com.remoteconsultation.model.PendingProfessionalReview;
import com.remoteconsultation.service.ProfessionalService;
import com.remoteconsultation.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminVerificationServlet extends HttpServlet {

    private static final String CSRF_SESSION_KEY =
        "ADMIN_VERIFICATION_CSRF_TOKEN";

    private static final SecureRandom SECURE_RANDOM =
        new SecureRandom();

        private String getOrCreateCsrfToken(HttpSession session) {
    String token = (String) session.getAttribute(CSRF_SESSION_KEY);

    if (token == null || token.isBlank()) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        session.setAttribute(CSRF_SESSION_KEY, token);
    }

    return token;
}

    private final ProfessionalService professionalService;

    public AdminVerificationServlet(
            ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = SessionUtil.getRole(session);

        request.setAttribute(
        "csrfToken",
        getOrCreateCsrfToken(session)
);

        // Defense in depth: only administrators may view this page.
        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.sendError(
                    session == null ? HttpServletResponse.SC_UNAUTHORIZED
                                    : HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            List<PendingProfessionalReview> pendingProfessionals =
                    professionalService.getPendingProfessionalReviews();

            request.setAttribute(
                    "pendingProfessionals", pendingProfessionals);

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/admin-verification.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load pending professional verifications.", e);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = SessionUtil.getRole(session);

        // Never trust a role supplied by the browser.
        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.sendError(
                    session == null ? HttpServletResponse.SC_UNAUTHORIZED
                                    : HttpServletResponse.SC_FORBIDDEN);
            return;
        }


String expectedToken =
        (String) session.getAttribute(CSRF_SESSION_KEY);

String submittedToken =
        request.getParameter("csrfToken");

if (expectedToken == null
        || submittedToken == null
        || !java.security.MessageDigest.isEqual(
                expectedToken.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8),
                submittedToken.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8))) {

    response.sendError(
            HttpServletResponse.SC_FORBIDDEN,
            "Invalid or missing CSRF token."
    );
    return;
}


        String professionalIdValue =
                request.getParameter("professionalId");
        String decision =
                request.getParameter("decision");

        if (professionalIdValue == null || decision == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final Long professionalId;

        try {
            professionalId = Long.parseLong(professionalIdValue);

            if (professionalId <= 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        decision = decision.trim().toUpperCase(java.util.Locale.ROOT);

        if (!"APPROVED".equals(decision)
                && !"REJECTED".equals(decision)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean updated =
                    professionalService.reviewProfessional(
                            professionalId, decision);

            String result;

            if (!updated) {
                result = "stale";
            } else {
                result = "APPROVED".equals(decision)
                        ? "approved" : "rejected";
            }

            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/verification?result=" + result);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to update professional verification.", e);
        }
    }
}
