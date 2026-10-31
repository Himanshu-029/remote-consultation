
package com.remoteconsultation.servlet;

import com.remoteconsultation.filter.CsrfProtectionFilter;
import com.remoteconsultation.model.PendingProfessionalReview;
import com.remoteconsultation.service.ProfessionalService;
import com.remoteconsultation.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class AdminVerificationServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(AdminVerificationServlet.class);

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

        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.sendError(
                    session == null
                            ? HttpServletResponse.SC_UNAUTHORIZED
                            : HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        request.setAttribute(
                "csrfToken",
                CsrfProtectionFilter.ensureToken(session)
        );

        try {
            List<PendingProfessionalReview> pendingProfessionals =
                    professionalService.getPendingProfessionalReviews();

            request.setAttribute(
                    "pendingProfessionals",
                    pendingProfessionals
            );

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/admin-verification.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            logger.error(
                    "Unable to load pending professional verifications.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load professional verifications. Please try again later."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = SessionUtil.getRole(session);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.sendError(
                    session == null
                            ? HttpServletResponse.SC_UNAUTHORIZED
                            : HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        // Validate the shared CSRF token.
        String expectedToken =
                (String) session.getAttribute(
                        CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE
                );

        String submittedToken = request.getParameter("_csrf");

        if (expectedToken == null
                || submittedToken == null
                || submittedToken.isBlank()
                || !MessageDigest.isEqual(
                        expectedToken.getBytes(StandardCharsets.UTF_8),
                        submittedToken.getBytes(StandardCharsets.UTF_8))) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid or missing CSRF token."
            );
            return;
        }

        String professionalIdValue =
                request.getParameter("professionalId");

        String decision = request.getParameter("decision");

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

        decision = decision.trim().toUpperCase(Locale.ROOT);

        if (!"APPROVED".equals(decision)
                && !"REJECTED".equals(decision)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean updated =
                    professionalService.reviewProfessional(
                            professionalId,
                            decision
                    );

            String result;

            if (!updated) {
                result = "stale";
            } else {
                result = "APPROVED".equals(decision)
                        ? "approved"
                        : "rejected";
            }

            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/verification?result=" + result
            );

        } catch (SQLException e) {
            logger.error(
                    "Unable to update professional verification. "
                            + "Professional ID: {}, Decision: {}",
                    professionalId,
                    decision,
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to update professional verification. Please try again later."
            );
        }
    }
}
