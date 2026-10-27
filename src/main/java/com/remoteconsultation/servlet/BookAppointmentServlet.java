
package com.remoteconsultation.servlet;

import com.remoteconsultation.service.AppointmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class BookAppointmentServlet extends HttpServlet {

    private final AppointmentService appointmentService;

    public BookAppointmentServlet(
            AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

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
        Long slotId;

        try {
            slotId = Long.valueOf(request.getParameter("slotId"));
            if (slotId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "A valid slot ID is required.");
            return;
        }

        String reason = request.getParameter("reason");

        try {
            Long appointmentId =
                    appointmentService.bookAppointment(
                            userId, slotId, reason);

            Long professionalId =
                    appointmentService.getProfessionalIdForAppointment(
                            appointmentId);

            if (professionalId == null) {
                throw new ServletException(
                        "The booked appointment could not be retrieved.");
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/patient/professional-availability?professionalId="
                    + professionalId
                    + "&booking=success&appointmentId="
                    + appointmentId);

        } catch (IllegalArgumentException e) {
            try {
                Long professionalId =
                        appointmentService.getProfessionalIdForSlot(slotId);

                if (professionalId == null) {
                    response.sendError(
                            HttpServletResponse.SC_CONFLICT,
                            "This slot is no longer available.");
                    return;
                }

                String message = URLEncoder.encode(
                        e.getMessage() == null
                                ? "Please choose another slot."
                                : e.getMessage(),
                        StandardCharsets.UTF_8);

                response.sendRedirect(
                        request.getContextPath()
                        + "/patient/professional-availability?professionalId="
                        + professionalId
                        + "&booking=error&message=" + message);

            } catch (SQLException lookupException) {
                throw new ServletException(
                        "Unable to retrieve the slot after booking failed.",
                        lookupException);
            }

        } catch (SecurityException e) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage());

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to complete the appointment booking.", e);
        }
    }
}
