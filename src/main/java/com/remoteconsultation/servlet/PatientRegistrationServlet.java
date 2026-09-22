package com.remoteconsultation.servlet;

import com.remoteconsultation.dto.RegistrationRequest;
import com.remoteconsultation.service.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

public class PatientRegistrationServlet extends HttpServlet {

    private final UserService userService;

    public PatientRegistrationServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(
                        "/WEB-INF/jsp/patient-register.jsp"
                );

        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

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

        String dateOfBirth =
                request.getParameter("dateOfBirth");

        if (dateOfBirth != null
                && !dateOfBirth.isBlank()) {

            try {
                registrationRequest.setDateOfBirth(
                        LocalDate.parse(dateOfBirth)
                );
            } catch (Exception e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid date of birth."
                );
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

            userService.registerPatient(
                    registrationRequest
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/login"
            );

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            throw new ServletException(
                    "Patient registration failed.",
                    e
            );
        }
    }
}