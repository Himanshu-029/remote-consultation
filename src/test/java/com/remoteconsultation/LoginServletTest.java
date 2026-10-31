
package com.remoteconsultation;

import com.remoteconsultation.filter.CsrfProtectionFilter;
import com.remoteconsultation.model.User;
import com.remoteconsultation.security.LoginRateLimiter;
import com.remoteconsultation.service.UserService;
import com.remoteconsultation.servlet.LoginServlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginServletTest {

    private TestableLoginServlet servlet;
    private UserService userService;
    private LoginRateLimiter loginRateLimiter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession oldSession;
    private HttpSession newSession;

    private static class TestableLoginServlet extends LoginServlet {

        TestableLoginServlet(
                UserService userService,
                LoginRateLimiter loginRateLimiter) {
            super(userService, loginRateLimiter);
        }

        void invokePost(
                HttpServletRequest request,
                HttpServletResponse response)
                throws ServletException, IOException {
            super.doPost(request, response);
        }

        void invokeGet(
                HttpServletRequest request,
                HttpServletResponse response)
                throws ServletException, IOException {
            super.doGet(request, response);
        }
    }

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        loginRateLimiter = new LoginRateLimiter();
        servlet = new TestableLoginServlet(
                userService,
                loginRateLimiter
        );

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        oldSession = mock(HttpSession.class);
        newSession = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("");
    }

    @Test
    void successfulLoginInvalidatesOldSessionAndCreatesNewSession()
            throws Exception {

        User user = new User();
        user.setUserId(42L);
        user.setFullName("Test Patient");
        user.setRole("PATIENT");

        when(request.getParameter("email"))
                .thenReturn("patient@example.com");
        when(request.getParameter("password"))
                .thenReturn("Test@12345");

        when(userService.login(any()))
                .thenReturn(user);

        when(request.getSession(false))
                .thenReturn(oldSession);

        when(request.getSession(true))
                .thenReturn(newSession);

        when(newSession.getAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
                .thenReturn(null);

        servlet.invokePost(request, response);

        verify(oldSession).invalidate();
        verify(newSession).setAttribute("userId", 42L);
        verify(newSession).setAttribute("role", "PATIENT");
        verify(newSession).setAttribute("userName", "Test Patient");

        verify(newSession).setAttribute(
                eq(CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE),
                argThat(token ->
                        token instanceof String
                                && !((String) token).isBlank())
        );

        verify(response).sendRedirect("/dashboard");
    }

    @Test
    void failedLoginDoesNotInvalidateExistingSession()
            throws Exception {

        when(request.getParameter("email"))
                .thenReturn("patient@example.com");
        when(request.getParameter("password"))
                .thenReturn("WrongPassword");

        when(userService.login(any()))
                .thenThrow(new IllegalArgumentException(
                        "Invalid email or password."
                ));

        when(request.getSession(false))
                .thenReturn(oldSession);

        servlet.invokePost(request, response);

        verify(response).sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid email or password."
        );

        verify(oldSession, never()).invalidate();
        verify(request, never()).getSession(true);
    }

    @Test
    void loginPageProvidesCsrfToken() throws Exception {
        RequestDispatcher dispatcher =
                mock(RequestDispatcher.class);

        when(request.getSession(true)).thenReturn(oldSession);
        when(oldSession.getAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
                .thenReturn(null);

        when(request.getRequestDispatcher(
                "/WEB-INF/jsp/login.jsp"))
                .thenReturn(dispatcher);

        servlet.invokeGet(request, response);

        verify(request).setAttribute(
                eq("csrfToken"),
                argThat(token ->
                        token instanceof String
                                && !((String) token).isBlank())
        );

        verify(dispatcher).forward(request, response);
    }
}
