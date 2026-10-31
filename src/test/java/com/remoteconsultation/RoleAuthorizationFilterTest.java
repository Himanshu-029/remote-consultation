
package com.remoteconsultation;

import com.remoteconsultation.filter.RoleAuthorizationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class RoleAuthorizationFilterTest {

    private RoleAuthorizationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RoleAuthorizationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        chain = mock(FilterChain.class);

        when(request.getRequestURI())
                .thenAnswer(invocation -> currentPath);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
    }

    private String currentPath = "/dashboard";

    private void setRole(String role) {
        when(session.getAttribute("role")).thenReturn(role);
    }

    private void setPath(String path) {
        currentPath = path;
    }

    private void runFilter() throws Exception {
        filter.doFilter(request, response, chain);
    }

    @Test
    void patientCannotAccessProfessionalEndpoint() throws Exception {
        setRole("PATIENT");
        setPath("/professional/appointments");

        runFilter();

        verify(response).sendError(
                eq(HttpServletResponse.SC_FORBIDDEN),
                anyString()
        );
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void professionalCannotAccessPatientEndpoint() throws Exception {
        setRole("PROFESSIONAL");
        setPath("/patient/appointments");

        runFilter();

        verify(response).sendError(
                eq(HttpServletResponse.SC_FORBIDDEN),
                anyString()
        );
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void patientCannotAccessAdminEndpoint() throws Exception {
        setRole("PATIENT");
        setPath("/admin/verification");

        runFilter();

        verify(response).sendError(
                eq(HttpServletResponse.SC_FORBIDDEN),
                anyString()
        );
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void adminCannotAccessPatientEndpoint() throws Exception {
        setRole("ADMIN");
        setPath("/patient/appointments");

        runFilter();

        verify(response).sendError(
                eq(HttpServletResponse.SC_FORBIDDEN),
                anyString()
        );
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void unknownRoleIsRejected() throws Exception {
        setRole("UNKNOWN");
        setPath("/dashboard");

        runFilter();

        verify(response).sendError(
                eq(HttpServletResponse.SC_FORBIDDEN),
                anyString()
        );
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void validPatientCanAccessDashboard() throws Exception {
        setRole("PATIENT");
        setPath("/dashboard");

        runFilter();

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void loginIsPublicWithoutSession() throws Exception {
        setPath("/login");
        when(request.getSession(false)).thenReturn(null);

        runFilter();

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void missingRoleIsRedirectedToLogin() throws Exception {
        when(session.getAttribute("role")).thenReturn(null);
        setPath("/dashboard");

        runFilter();

        verify(response).sendRedirect("/login");
        verify(chain, never()).doFilter(request, response);
    }
}
