
package com.remoteconsultation.servlet;

import com.remoteconsultation.service.AppointmentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class CancelAppointmentServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(CancelAppointmentServlet.class);

    private final AppointmentService appointmentService;

    public CancelAppointmentServlet(
            AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !"PATIENT".equalsIgnoreCase(
                        String.valueOf(session.getAttribute("role")))
                || !(session.getAttribute("userId") instanceof Long)) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please log in as a patient.");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");

        Long appointmentId;

        try {
            appointmentId = Long.valueOf(
                    request.getParameter("appointmentId"));

            if (appointmentId <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException | NullPointerException e) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/patient/appointments?cancel=error");
            return;
        }

        try {
            appointmentService.cancelPatientAppointment(
                    userId, appointmentId);

            response.sendRedirect(
                    request.getContextPath()
                            + "/patient/appointments?cancel=success");

        } catch (IllegalArgumentException e) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/patient/appointments?cancel=error");

        } catch (SecurityException e) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to cancel this appointment.");

        } catch (SQLException e) {
            logger.error(
                    "Database error while cancelling appointment. "
                            + "Patient ID: {}, Appointment ID: {}",
                    userId,
                    appointmentId,
                    e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "We could not cancel your appointment. "
                            + "Please check your appointments before trying again.");
        }
    }
}
