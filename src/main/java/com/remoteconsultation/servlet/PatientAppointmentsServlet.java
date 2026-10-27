
package com.remoteconsultation.servlet;

import com.remoteconsultation.model.Appointment;
import com.remoteconsultation.service.AppointmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PatientAppointmentsServlet extends HttpServlet {

    private final AppointmentService appointmentService;

    public PatientAppointmentsServlet(
            AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !"PATIENT".equalsIgnoreCase(
                        String.valueOf(session.getAttribute("role")))
                || !(session.getAttribute("userId") instanceof Long)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "A logged-in patient account is required.");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");

        try {
            List<Appointment> appointments =
                    appointmentService.getAppointmentsByPatientId(userId);

            request.setAttribute("appointments", appointments);

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/patient-appointments.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your appointments.", e);
        }
    }
}
