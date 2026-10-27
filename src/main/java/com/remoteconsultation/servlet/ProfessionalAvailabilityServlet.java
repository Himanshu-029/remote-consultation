package com.remoteconsultation.servlet;

import com.remoteconsultation.service.AvailabilitySlotService;
import com.remoteconsultation.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ProfessionalAvailabilityServlet extends HttpServlet {

    private final AvailabilitySlotService availabilitySlotService;

    public ProfessionalAvailabilityServlet(
            AvailabilitySlotService availabilitySlotService) {
        this.availabilitySlotService = availabilitySlotService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/jsp/professional-availability.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Long professionalId = SessionUtil.getUserId(session);

        if (professionalId == null
                || !"PROFESSIONAL".equalsIgnoreCase(
                        SessionUtil.getRole(session))) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Professional access is required."
            );
            return;
        }

        try {
            LocalDate slotDate = LocalDate.parse(
                    request.getParameter("slotDate"));

            LocalTime startTime = LocalTime.parse(
                    request.getParameter("startTime"));

            LocalTime endTime = LocalTime.parse(
                    request.getParameter("endTime"));

            Long slotId = availabilitySlotService.createSlot(
                    professionalId,
                    slotDate,
                    startTime,
                    endTime
            );

            request.setAttribute(
                    "successMessage",
                    "Availability created successfully. Slot ID: " + slotId
            );

        } catch (DateTimeParseException e) {
            request.setAttribute(
                    "errorMessage",
                    "Enter a valid date and time."
            );

        } catch (IllegalArgumentException | SecurityException e) {
            request.setAttribute(
                    "errorMessage",
                    e.getMessage()
            );

        } catch (java.sql.SQLException e) {
            log("Failed to create professional availability.", e);
            request.setAttribute(
                    "errorMessage",
                    "Unable to save availability. Check for duplicate slots and try again."
            );
        }

        request.getRequestDispatcher(
                "/WEB-INF/jsp/professional-availability.jsp"
        ).forward(request, response);
    }
}
