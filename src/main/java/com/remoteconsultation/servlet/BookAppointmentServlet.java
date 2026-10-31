
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class BookAppointmentServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(BookAppointmentServlet.class);

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
                logger.error(
                        "Professional lookup failed after booking. "
                                + "Appointment ID: {}",
                        appointmentId);

                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Your appointment may have been booked, "
                                + "but its details could not be retrieved. "
                                + "Please check your appointments.");
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                            + "/patient/professional-availability?professionalId="
                            + professionalId
                            + "&booking=success&appointmentId="
                            + appointmentId);

        } catch (IllegalArgumentException e) {
            handleBookingValidationError(
                    request, response, slotId, e);

        } catch (SecurityException e) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not permitted to book this appointment.");

        } catch (SQLException e) {
            logger.error(
                    "Database error during appointment booking. "
                            + "Patient ID: {}, Slot ID: {}",
                    userId,
                    slotId,
                    e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "We could not complete your booking. "
                            + "Please check your appointments before trying again.");
        }
    }

    private void handleBookingValidationError(
            HttpServletRequest request,
            HttpServletResponse response,
            Long slotId,
            IllegalArgumentException bookingException)
            throws ServletException, IOException {

        try {
            Long professionalId =
                    appointmentService.getProfessionalIdForSlot(slotId);

            if (professionalId == null) {
                response.sendError(
                        HttpServletResponse.SC_CONFLICT,
                        "This slot is no longer available.");
                return;
            }

            // Do not send arbitrary exception messages to the client.
            String safeMessage =
                    "Your booking could not be completed. "
                            + "Please check the slot and try again.";

            String encodedMessage = URLEncoder.encode(
                    safeMessage,
                    StandardCharsets.UTF_8);

            response.sendRedirect(
                    request.getContextPath()
                            + "/patient/professional-availability?professionalId="
                            + professionalId
                            + "&booking=error&message="
                            + encodedMessage);

        } catch (SQLException lookupException) {
            logger.error(
                    "Unable to retrieve the slot after a booking failure. "
                            + "Slot ID: {}",
                    slotId,
                    lookupException);

            throw new ServletException(
                    "Unable to retrieve appointment slot details.",
                    lookupException);
        }
    }
}
