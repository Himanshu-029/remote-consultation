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
import com.remoteconsultation.servlet.AdminVerificationServlet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.remoteconsultation.servlet.ProfessionalAvailabilityServlet;
import com.remoteconsultation.service.AvailabilitySlotService;
import com.remoteconsultation.servlet.ProfessionalDirectoryServlet;
import com.remoteconsultation.service.ProfessionalDirectoryService;
import com.remoteconsultation.servlet.PatientProfessionalAvailabilityServlet;
import com.remoteconsultation.servlet.BookAppointmentServlet;
import com.remoteconsultation.service.AppointmentService;
import com.remoteconsultation.servlet.PatientAppointmentsServlet;
import com.remoteconsultation.servlet.ProfessionalAppointmentsServlet;
import com.remoteconsultation.servlet.CancelAppointmentServlet;

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

    @Bean
        public ServletRegistrationBean<ProfessionalAppointmentsServlet>
                professionalAppointmentsServlet(
                        AppointmentService appointmentService) {

        ServletRegistrationBean<ProfessionalAppointmentsServlet> bean =
                new ServletRegistrationBean<>(
                        new ProfessionalAppointmentsServlet(appointmentService),
                        "/professional/appointments");

        bean.setName("professionalAppointmentsServlet");
        return bean;
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

    // ---------------------------------------------------------
// Admin Professional Verification Servlet
// ---------------------------------------------------------
@Bean
public ServletRegistrationBean<AdminVerificationServlet>
adminVerificationServlet(ProfessionalService professionalService) {

    AdminVerificationServlet servlet =
            new AdminVerificationServlet(professionalService);

    return new ServletRegistrationBean<>(
            servlet,
            "/admin/verification"
    );
}


    // ---------------------------------------------------------
    // Professional Availability Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<ProfessionalAvailabilityServlet>
    professionalAvailabilityServlet(
            AvailabilitySlotService availabilitySlotService) {

        ProfessionalAvailabilityServlet servlet =
                new ProfessionalAvailabilityServlet(
                        availabilitySlotService
                );

        return new ServletRegistrationBean<>(
                servlet,
                "/professional/availability"
        );
    }


    // ---------------------------------------------------------
    // Patient Professional Directory Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<ProfessionalDirectoryServlet>
    professionalDirectoryServlet(
            ProfessionalDirectoryService directoryService) {

        ProfessionalDirectoryServlet servlet =
                new ProfessionalDirectoryServlet(directoryService);

        return new ServletRegistrationBean<>(
                servlet,
                "/patient/professionals"
        );
    }


    // ---------------------------------------------------------
    // Patient Professional Availability Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<PatientProfessionalAvailabilityServlet>
    patientProfessionalAvailabilityServlet(
            AvailabilitySlotService availabilitySlotService) {

        PatientProfessionalAvailabilityServlet servlet =
                new PatientProfessionalAvailabilityServlet(
                        availabilitySlotService);

        return new ServletRegistrationBean<>(
                servlet,
                "/patient/professional-availability");
    }


    // ---------------------------------------------------------
    // Patient Appointment Booking Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<BookAppointmentServlet>
    bookAppointmentServlet(AppointmentService appointmentService) {

        BookAppointmentServlet servlet =
                new BookAppointmentServlet(appointmentService);

        return new ServletRegistrationBean<>(
                servlet,
                "/patient/book-appointment");
    }



    // ---------------------------------------------------------
    // Patient Appointment History Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<PatientAppointmentsServlet>
    patientAppointmentsServlet(
            AppointmentService appointmentService) {

        PatientAppointmentsServlet servlet =
                new PatientAppointmentsServlet(appointmentService);

        return new ServletRegistrationBean<>(
                servlet,
                "/patient/appointments"
        );
    }


    // ---------------------------------------------------------
    // Patient Appointment Cancellation Servlet
    // ---------------------------------------------------------
    @Bean
    public ServletRegistrationBean<CancelAppointmentServlet>
    cancelAppointmentServlet(
            AppointmentService appointmentService) {

        CancelAppointmentServlet servlet =
                new CancelAppointmentServlet(
                        appointmentService);

        return new ServletRegistrationBean<>(
                servlet,
                "/patient/cancel-appointment");
    }


}