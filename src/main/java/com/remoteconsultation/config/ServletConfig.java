package com.remoteconsultation.config;

import com.remoteconsultation.filter.AuthenticationFilter;
import com.remoteconsultation.filter.RoleAuthorizationFilter;
import com.remoteconsultation.listener.ApplicationStartupListener;
import com.remoteconsultation.service.ProfessionalService;
import com.remoteconsultation.service.UserService;
import com.remoteconsultation.servlet.DashboardServlet;
import com.remoteconsultation.servlet.LoginServlet;
import com.remoteconsultation.servlet.LogoutServlet;
import com.remoteconsultation.servlet.PatientRegistrationServlet;
import com.remoteconsultation.servlet.ProfessionalRegistrationServlet;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfig {

    // ---------------------------------------------------------
    // Patient Registration Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<PatientRegistrationServlet>
    patientRegistrationServlet(UserService userService) {

        PatientRegistrationServlet servlet =
                new PatientRegistrationServlet(userService);

        return new ServletRegistrationBean<>(
                servlet,
                "/register"
        );
    }

    // ---------------------------------------------------------
    // Professional Registration Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<ProfessionalRegistrationServlet>
    professionalRegistrationServlet(
            ProfessionalService professionalService) {

        ProfessionalRegistrationServlet servlet =
                new ProfessionalRegistrationServlet(
                        professionalService
                );

        return new ServletRegistrationBean<>(
                servlet,
                "/professional-register"
        );
    }

    // ---------------------------------------------------------
    // Application Startup Listener
    // ---------------------------------------------------------
    @Bean
    public ServletListenerRegistrationBean<ApplicationStartupListener>
    applicationStartupListener() {

        return new ServletListenerRegistrationBean<>(
                new ApplicationStartupListener()
        );
    }

    // ---------------------------------------------------------
    // Authentication Filter
    // ---------------------------------------------------------
    @Bean
    public FilterRegistrationBean<AuthenticationFilter>
    authenticationFilter() {

        FilterRegistrationBean<AuthenticationFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(
                new AuthenticationFilter()
        );

        registration.addUrlPatterns("/*");
        registration.setOrder(1);

        return registration;
    }

    // ---------------------------------------------------------
    // Role Authorization Filter
    // ---------------------------------------------------------
    @Bean
    public FilterRegistrationBean<RoleAuthorizationFilter>
    roleAuthorizationFilter() {

        FilterRegistrationBean<RoleAuthorizationFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(
                new RoleAuthorizationFilter()
        );

        registration.addUrlPatterns("/*");
        registration.setOrder(2);

        return registration;
    }

    // ---------------------------------------------------------
    // Logout Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<LogoutServlet>
    logoutServlet() {

        LogoutServlet servlet =
                new LogoutServlet();

        return new ServletRegistrationBean<>(
                servlet,
                "/logout"
        );
    }

    // ---------------------------------------------------------
    // Dashboard Servlet
    // ---------------------------------------------------------
   @Bean
public ServletRegistrationBean<DashboardServlet>
dashboardServlet(ProfessionalService professionalService) {

    DashboardServlet servlet =
            new DashboardServlet(professionalService);

    return new ServletRegistrationBean<>(
            servlet,
            "/dashboard"
    );
}
    // ---------------------------------------------------------
    // Login Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<LoginServlet>
    loginServlet(UserService userService) {

        LoginServlet servlet =
                new LoginServlet(userService);

        return new ServletRegistrationBean<>(
                servlet,
                "/login"
        );
    }
}