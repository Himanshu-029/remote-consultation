package com.remoteconsultation.servlet;

import com.remoteconsultation.dto.LoginRequest;
import com.remoteconsultation.model.User;
import com.remoteconsultation.service.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private final UserService userService;

    public LoginServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(
                        "/WEB-INF/jsp/login.jsp"
                );

        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setEmail(
                request.getParameter("email")
        );

        loginRequest.setPassword(
                request.getParameter("password")
        );

        try {

            User user = userService.login(loginRequest);

            /*
             * Create a fresh session after successful authentication.
             * This prevents session fixation by ensuring that the
             * authenticated session is not the pre-login session.
             */
            HttpSession oldSession =
                    request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session =
                    request.getSession(true);

            session.setAttribute(
                    "userId",
                    user.getUserId()
            );

            session.setAttribute(
                    "role",
                    user.getRole()
            );

            session.setAttribute(
                    "userName",
                    user.getFullName()
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/dashboard"
            );

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    e.getMessage()
            );

        } catch (Exception e) {

            throw new ServletException(
                    "Login failed.",
                    e
            );
        }
    }
}