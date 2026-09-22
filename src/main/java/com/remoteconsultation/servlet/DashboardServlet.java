package com.remoteconsultation.servlet;

import com.remoteconsultation.util.SessionUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        Long userId =
                SessionUtil.getUserId(session);

        String role =
                SessionUtil.getRole(session);

        String userName =
                SessionUtil.getUserName(session);

        

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(
                        "/WEB-INF/jsp/dashboard.jsp"
                );

        dispatcher.forward(request, response);
    }
}