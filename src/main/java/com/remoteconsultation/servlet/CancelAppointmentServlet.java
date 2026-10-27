
package com.remoteconsultation.servlet;

import com.remoteconsultation.service.AppointmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

public class CancelAppointmentServlet extends HttpServlet {

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
                || !"PATIENT".equals(session.getAttribute("role"))
                || !(session.getAttribute("userId") instanceof Long)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please log in as a patient.");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");

        try {
            Long appointmentId = Long.valueOf(
                    request.getParameter("appointmentId"));

            appointmentService.cancelPatientAppointment(
                    userId, appointmentId);

            response.sendRedirect(
                    request.getContextPath()
                            + "/patient/appointments?cancel=success");

        } catch (NumberFormatException | NullPointerException e) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/patient/appointments?cancel=error");

        } catch (IllegalArgumentException e) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/patient/appointments?cancel=error");

        } catch (SecurityException e) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to cancel this appointment.");

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to cancel appointment.", e);
        }
    }
}
