
package com.remoteconsultation.servlet;

import com.remoteconsultation.model.AvailabilitySlot;
import com.remoteconsultation.service.AvailabilitySlotService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PatientProfessionalAvailabilityServlet
        extends HttpServlet {

    private final AvailabilitySlotService availabilitySlotService;

    public PatientProfessionalAvailabilityServlet(
            AvailabilitySlotService availabilitySlotService) {
        this.availabilitySlotService = availabilitySlotService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String professionalIdParam =
                request.getParameter("professionalId");

        long professionalId;

        try {
            professionalId = Long.parseLong(professionalIdParam);

            if (professionalId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "A valid professional ID is required.");
            return;
        }

        try {
            List<AvailabilitySlot> slots =
                    availabilitySlotService.getAvailableSlots(
                            professionalId);

            request.setAttribute("professionalId", professionalId);
            request.setAttribute("slots", slots);

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/patient-professional-availability.jsp")
                    .forward(request, response);

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage());
        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load professional availability.", e);
        }
    }
}
