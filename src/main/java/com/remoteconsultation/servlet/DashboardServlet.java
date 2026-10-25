package com.remoteconsultation.servlet;

import com.remoteconsultation.service.ProfessionalService;
import com.remoteconsultation.util.SessionUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardServlet extends HttpServlet {

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

        if ("PROFESSIONAL".equals(role)) {
            try {
                String verificationStatus =
                        professionalService
                                .getProfessionalVerificationStatus(userId);

                request.setAttribute(
                        "verificationStatus",
                        verificationStatus
                );

            } catch (SQLException e) {
                throw new ServletException(
                        "Unable to retrieve professional verification status.",
                        e
                );
            }
        }

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(
                        "/WEB-INF/jsp/dashboard.jsp"
                );

        dispatcher.forward(request, response);
    }
}