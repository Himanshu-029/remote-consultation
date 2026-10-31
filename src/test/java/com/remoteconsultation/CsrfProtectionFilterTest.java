package com.remoteconsultation;

import com.remoteconsultation.filter.CsrfProtectionFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CsrfProtectionFilterTest {

    private CsrfProtectionFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new CsrfProtectionFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(session);
    }

    @Test
    void generatesAndReusesSessionToken() {
        when(session.getAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
                .thenReturn(null);

        String token = CsrfProtectionFilter.ensureToken(session);

        assertNotNull(token);
        assertFalse(token.isBlank());

        verify(session).setAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE,
                token);
    }

    @Test
    void rejectsPostWithoutSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF validation failed. Please refresh the page and try again.");
        verifyNoInteractions(chain);
    }

    @Test
    void rejectsPostWithoutToken() throws Exception {
        when(session.getAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
                .thenReturn("expected-token");
        when(request.getParameter(
                CsrfProtectionFilter.REQUEST_TOKEN_PARAMETER))
                .thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF validation failed. Please refresh the page and try again.");
        verifyNoInteractions(chain);
    }

    @Test
    void rejectsInvalidToken() throws Exception {
        when(session.getAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
                .thenReturn("expected-token");
        when(request.getParameter(
                CsrfProtectionFilter.REQUEST_TOKEN_PARAMETER))
                .thenReturn("wrong-token");

        filter.doFilter(request, response, chain);

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF validation failed. Please refresh the page and try again.");
        verifyNoInteractions(chain);
    }

    @Test
    void allowsPostWithValidToken() throws Exception {
        String token = "valid-test-token";

        when(session.getAttribute(
                CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
                .thenReturn(token);
        when(request.getParameter(
                CsrfProtectionFilter.REQUEST_TOKEN_PARAMETER))
                .thenReturn(token);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(
                eq(HttpServletResponse.SC_FORBIDDEN),
                anyString());
    }


@Test
void allowsGetWithoutCsrfToken() throws Exception {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/login");
    when(request.getContextPath()).thenReturn("");
    when(request.getSession(true)).thenReturn(session);
    when(session.getAttribute(
            CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
            .thenReturn(null);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verify(response, never())
            .sendError(eq(403), anyString());
    verify(session).setAttribute(
            eq(CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE),
            anyString());
}



@Test
void allowsHeadWithoutCsrfToken() throws Exception {
    when(request.getMethod()).thenReturn("HEAD");
    when(request.getRequestURI()).thenReturn("/dashboard");
    when(request.getContextPath()).thenReturn("");
    when(request.getSession(true)).thenReturn(session);
    when(session.getAttribute(
            CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
            .thenReturn(null);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verify(response, never()).sendError(
            eq(HttpServletResponse.SC_FORBIDDEN),
            anyString()
    );
}

@Test
void rejectsTraceWithoutValidCsrfToken() throws Exception {
    when(request.getMethod()).thenReturn("TRACE");
    when(session.getAttribute(
            CsrfProtectionFilter.SESSION_TOKEN_ATTRIBUTE))
            .thenReturn("expected-token");
    when(request.getParameter(
            CsrfProtectionFilter.REQUEST_TOKEN_PARAMETER))
            .thenReturn(null);

    filter.doFilter(request, response, chain);

    verify(response).sendError(
            eq(HttpServletResponse.SC_FORBIDDEN),
            anyString()
    );
    verifyNoInteractions(chain);
}


}
