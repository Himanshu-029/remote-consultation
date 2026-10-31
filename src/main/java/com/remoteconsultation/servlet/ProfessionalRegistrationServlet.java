
package com.remoteconsultation.servlet;

import com.remoteconsultation.dto.ProfessionalRegistrationRequest;
import com.remoteconsultation.filter.CsrfProtectionFilter;
import com.remoteconsultation.service.ProfessionalService;

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

public class ProfessionalRegistrationServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ProfessionalRegistrationServlet.class
            );

    private final ProfessionalService professionalService;

    public ProfessionalRegistrationServlet(
            ProfessionalService professionalService) {

        this.professionalService = professionalService;
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
                        "/WEB-INF/jsp/professional-register.jsp"
                );

        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        ProfessionalRegistrationRequest registrationRequest =
                new ProfessionalRegistrationRequest();

        registrationRequest.setFullName(
                request.getParameter("fullName")
        );

        registrationRequest.setEmail(
                request.getParameter("email")
        );

        registrationRequest.setPassword(
                request.getParameter("password")
        );

        registrationRequest.setPhone(
                request.getParameter("phone")
        );

        // Specialization
        String specializationId =
                request.getParameter("specializationId");

        if (specializationId != null
                && !specializationId.isBlank()) {

            try {
                registrationRequest.setSpecializationId(
                        Long.parseLong(specializationId)
                );

            } catch (NumberFormatException e) {
                request.setAttribute(
                        "error",
                        "Invalid specialization selected."
                );

                forwardToRegistrationPage(request, response);
                return;
            }
        }

        // Professional details
        registrationRequest.setQualification(
                request.getParameter("qualification")
        );

        registrationRequest.setLicenseNumber(
                request.getParameter("licenseNumber")
        );

        // Experience
        String experienceYears =
                request.getParameter("experienceYears");

        if (experienceYears != null
                && !experienceYears.isBlank()) {

            try {
                registrationRequest.setExperienceYears(
                        Integer.parseInt(experienceYears)
                );

            } catch (NumberFormatException e) {
                request.setAttribute(
                        "error",
                        "Experience years must be a valid number."
                );

                forwardToRegistrationPage(request, response);
                return;
            }
        }

        // Consultation fee
        registrationRequest.setConsultationFee(
                request.getParameter("consultationFee")
        );

        // Professional biography
        registrationRequest.setBio(
                request.getParameter("bio")
        );

        // Register professional
        try {
            Long userId =
                    professionalService.registerProfessional(
                            registrationRequest
                    );

            request.setAttribute(
                    "success",
                    "Professional registration successful. "
                            + "Your account is pending admin verification."
            );

            request.setAttribute("userId", userId);

            forwardToRegistrationPage(request, response);

        } catch (IllegalArgumentException e) {
            // Expected validation errors: show the safe message.
            request.setAttribute("error", e.getMessage());

            forwardToRegistrationPage(request, response);

        } catch (SQLException e) {
            // Keep technical details in server logs, not the response.
            logger.error(
                    "Database error during professional registration.",
                    e
            );

            request.setAttribute(
                    "error",
                    "Registration failed due to a database error."
            );

            forwardToRegistrationPage(request, response);

        } catch (Exception e) {
            logger.error(
                    "Unexpected error during professional registration.",
                    e
            );

            request.setAttribute(
                    "error",
                    "An unexpected error occurred during registration."
            );

            forwardToRegistrationPage(request, response);
        }
    }

    private void forwardToRegistrationPage(
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
                        "/WEB-INF/jsp/professional-register.jsp"
                );

        dispatcher.forward(request, response);
    }
}
