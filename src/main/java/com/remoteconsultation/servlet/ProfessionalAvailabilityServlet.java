
package com.remoteconsultation.servlet;

import com.remoteconsultation.service.AvailabilitySlotService;
import com.remoteconsultation.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
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

        HttpSession session = request.getSession(false);

        Long professionalId = SessionUtil.getUserId(session);
        String role = SessionUtil.getRole(session);

        if (professionalId == null
                || !"PROFESSIONAL".equalsIgnoreCase(role)) {
            response.sendError(
                    session == null
                            ? HttpServletResponse.SC_UNAUTHORIZED
                            : HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

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
        String role = SessionUtil.getRole(session);

        if (professionalId == null
                || !"PROFESSIONAL".equalsIgnoreCase(role)) {
            response.sendError(
                    session == null
                            ? HttpServletResponse.SC_UNAUTHORIZED
                            : HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        try {
            String slotDateValue = request.getParameter("slotDate");
            String startTimeValue = request.getParameter("startTime");
            String endTimeValue = request.getParameter("endTime");

            if (slotDateValue == null
                    || startTimeValue == null
                    || endTimeValue == null
                    || slotDateValue.isBlank()
                    || startTimeValue.isBlank()
                    || endTimeValue.isBlank()) {
                request.setAttribute(
                        "errorMessage",
                        "Date, start time, and end time are required."
                );
                forwardToPage(request, response);
                return;
            }

            LocalDate slotDate = LocalDate.parse(slotDateValue);
            LocalTime startTime = LocalTime.parse(startTimeValue);
            LocalTime endTime = LocalTime.parse(endTimeValue);

            Long slotId = availabilitySlotService.createSlot(
                    professionalId,
                    slotDate,
                    startTime,
                    endTime
            );

            request.setAttribute(
                    "successMessage",
                    "Availability created successfully."
            );

        } catch (DateTimeParseException e) {
            request.setAttribute(
                    "errorMessage",
                    "Enter a valid date and time."
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute(
                    "errorMessage",
                    "The availability details are invalid. Check the date and time and try again."
            );

        } catch (SecurityException e) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to perform this action."
            );
            return;

        } catch (SQLException e) {
            log(
                    "Failed to create availability for professional ID "
                            + professionalId,
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to save availability. Please try again later."
            );
            return;
        }

        forwardToPage(request, response);
    }

    private void forwardToPage(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/jsp/professional-availability.jsp"
        ).forward(request, response);
    }
}
