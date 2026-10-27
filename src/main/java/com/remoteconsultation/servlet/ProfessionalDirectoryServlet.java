
package com.remoteconsultation.servlet;

import com.remoteconsultation.model.ProfessionalDirectoryItem;
import com.remoteconsultation.service.ProfessionalDirectoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProfessionalDirectoryServlet extends HttpServlet {

    private final ProfessionalDirectoryService directoryService;

    public ProfessionalDirectoryServlet(
            ProfessionalDirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<ProfessionalDirectoryItem> professionals =
                    directoryService.getApprovedProfessionals();

            request.setAttribute("professionals", professionals);

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/professional-directory.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load the professional directory.", e
            );
        }
    }
}
