
package com.remoteconsultation.servlet;

import com.remoteconsultation.service.ProfessionalService;
import com.remoteconsultation.util.SessionUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

public class DashboardServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(DashboardServlet.class);

    private final ProfessionalService professionalService;

    public DashboardServlet(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        Long userId = SessionUtil.getUserId(session);
        String role = SessionUtil.getRole(session);

        if (userId == null || role == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if ("PROFESSIONAL".equals(role.toUpperCase(Locale.ROOT))) {
            try {
                String verificationStatus =
                        professionalService
                                .getProfessionalVerificationStatus(userId);

                request.setAttribute(
                        "verificationStatus",
                        verificationStatus
                );

            } catch (SQLException e) {
                logger.error(
                        "Unable to retrieve professional verification status "
                                + "for user ID {}.",
                        userId,
                        e
                );

                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to load your dashboard. Please try again later."
                );
                return;
            }
        }

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(
                        "/WEB-INF/jsp/dashboard.jsp"
                );

        dispatcher.forward(request, response);
    }
}
