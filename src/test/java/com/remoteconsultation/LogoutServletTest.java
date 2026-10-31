
package com.remoteconsultation;

import com.remoteconsultation.filter.CsrfProtectionFilter;
import com.remoteconsultation.servlet.LogoutServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class LogoutServletTest {

    private TestableLogoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    /*
     * Test-only subclass that exposes the protected servlet methods.
     * Production code remains unchanged.
     */
    private static class TestableLogoutServlet extends LogoutServlet {

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
        servlet = new TestableLogoutServlet();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("");
    }

    @Test
    void successfulPostInvalidatesSession() throws Exception {
        String token = "valid-test-token";

        when(request.getSession(false)).thenReturn(session);

        when(session.getAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
                .thenReturn(token);

        when(request.getParameter("_csrf")).thenReturn(token);

        servlet.invokePost(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/login");
    }

    @Test
    void postWithoutSessionRedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.invokePost(request, response);

        verify(response).sendRedirect("/login");
        verify(session, never()).invalidate();
    }

    @Test
    void postWithInvalidTokenDoesNotInvalidateSession()
            throws Exception {

        when(request.getSession(false)).thenReturn(session);

        when(session.getAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
                .thenReturn("expected-token");

        when(request.getParameter("_csrf"))
                .thenReturn("wrong-token");

        servlet.invokePost(request, response);

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Invalid or missing CSRF token."
        );

        verify(session, never()).invalidate();
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void getLogoutIsRejected() throws Exception {
        servlet.invokeGet(request, response);

        verify(response).setHeader("Allow", "POST");

        verify(response).sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Logout requires POST."
        );

        verifyNoInteractions(session);
    }
}
