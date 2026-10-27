
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

public class ProfessionalAppointmentsServlet extends HttpServlet {

    private final AppointmentService appointmentService;

    public ProfessionalAppointmentsServlet(
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
                || !"PROFESSIONAL".equalsIgnoreCase(
                        String.valueOf(session.getAttribute("role")))
                || !(session.getAttribute("userId") instanceof Long)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "A logged-in professional account is required.");
            return;
        }

        Long professionalId = (Long) session.getAttribute("userId");

        try {
            List<Appointment> appointments =
                    appointmentService.getAppointmentsByProfessionalId(
                            professionalId);

            request.setAttribute("appointments", appointments);

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/professional-appointments.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load professional appointments.", e);
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || !"PROFESSIONAL".equalsIgnoreCase(
                        String.valueOf(session.getAttribute("role")))
                || !(session.getAttribute("userId") instanceof Long)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "A logged-in professional account is required.");
            return;
        }

        Long professionalId = (Long) session.getAttribute("userId");

        try {
            Long appointmentId =
                    Long.valueOf(request.getParameter("appointmentId"));

            String action = request.getParameter("action");

            if ("confirm".equals(action)) {
                appointmentService.updateAppointmentStatus(
                        professionalId, appointmentId, "CONFIRMED");

                response.sendRedirect(
                        request.getContextPath()
                        + "/professional/appointments?updated=confirmed");

            } else if ("reject".equals(action)) {
                appointmentService.updateAppointmentStatus(
                        professionalId, appointmentId, "REJECTED");

                response.sendRedirect(
                        request.getContextPath()
                        + "/professional/appointments?updated=rejected");

            } else if ("complete".equals(action)) {
                appointmentService.completeConfirmedAppointment(
                        professionalId, appointmentId);

                response.sendRedirect(
                        request.getContextPath()
                        + "/professional/appointments?updated=completed");

            } else {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid appointment action.");
            }

        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "A valid appointment ID is required.");

        } catch (IllegalArgumentException e) {
    String message = java.net.URLEncoder.encode(
            e.getMessage(),
            java.nio.charset.StandardCharsets.UTF_8);

    response.sendRedirect(
            request.getContextPath()
                    + "/professional/appointments?error="
                    + message);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to update appointment status.", e);
        }
    }


}
