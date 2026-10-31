
package com.remoteconsultation.servlet;

import com.remoteconsultation.dto.RegistrationRequest;
import com.remoteconsultation.filter.CsrfProtectionFilter;
import com.remoteconsultation.service.UserService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PatientRegistrationServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    PatientRegistrationServlet.class
            );

    private final UserService userService;

    public PatientRegistrationServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        forwardToRegistrationPage(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        RegistrationRequest registrationRequest =
                new RegistrationRequest();

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

        String dateOfBirth = request.getParameter("dateOfBirth");

        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            try {
                LocalDate parsedDate = LocalDate.parse(dateOfBirth);

                if (parsedDate.isAfter(LocalDate.now())) {
                    request.setAttribute(
                            "error",
                            "Date of birth cannot be in the future."
                    );

                    forwardToRegistrationPage(request, response);
                    return;
                }

                registrationRequest.setDateOfBirth(parsedDate);

            } catch (DateTimeParseException e) {
                request.setAttribute(
                        "error",
                        "Please provide a valid date of birth."
                );

                forwardToRegistrationPage(request, response);
                return;
            }
        }

        registrationRequest.setGender(
                request.getParameter("gender")
        );

        registrationRequest.setBloodGroup(
                request.getParameter("bloodGroup")
        );

        registrationRequest.setAddress(
                request.getParameter("address")
        );

        registrationRequest.setEmergencyContact(
                request.getParameter("emergencyContact")
        );

        try {
            userService.registerPatient(registrationRequest);

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

        } catch (IllegalArgumentException e) {
            // Service validation errors are expected and user-facing.
            request.setAttribute(
                    "error",
                    e.getMessage()
            );

            forwardToRegistrationPage(request, response);

        } catch (Exception e) {
            logger.error(
                    "Unexpected error during patient registration."
            , e);

            request.setAttribute(
                    "error",
                    "Registration could not be completed. Please try again later."
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
                        "/WEB-INF/jsp/patient-register.jsp"
                );

        dispatcher.forward(request, response);
    }
}
