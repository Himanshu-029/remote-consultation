package com.remoteconsultation.config;
import com.remoteconsultation.servlet.DashboardServlet;
import com.remoteconsultation.service.UserService;
import com.remoteconsultation.servlet.PatientRegistrationServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.remoteconsultation.servlet.LoginServlet;
import com.remoteconsultation.servlet.LogoutServlet;
import com.remoteconsultation.filter.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import com.remoteconsultation.filter.RoleAuthorizationFilter;
import com.remoteconsultation.listener.ApplicationStartupListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import com.remoteconsultation.servlet.ProfessionalRegistrationServlet;


@Configuration
public class ServletConfig {

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

    @Bean
public ServletRegistrationBean<ProfessionalRegistrationServlet>
professionalRegistrationServlet() {

    ProfessionalRegistrationServlet servlet =
            new ProfessionalRegistrationServlet();

    return new ServletRegistrationBean<>(
            servlet,
            "/professional-register"
    );
}



    @Bean
public ServletListenerRegistrationBean<ApplicationStartupListener>
applicationStartupListener() {

    return new ServletListenerRegistrationBean<>(
            new ApplicationStartupListener()
    );
}

    @Bean
public FilterRegistrationBean<RoleAuthorizationFilter>
roleAuthorizationFilter() {

    FilterRegistrationBean<RoleAuthorizationFilter> registration =
            new FilterRegistrationBean<>();

    registration.setFilter(new RoleAuthorizationFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(2);

    return registration;
}


    @Bean
    public FilterRegistrationBean<AuthenticationFilter>
    authenticationFilter() {

    FilterRegistrationBean<AuthenticationFilter> registration =
            new FilterRegistrationBean<>();

    registration.setFilter(new AuthenticationFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(1);

    return registration;
}


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

    @Bean
    public ServletRegistrationBean<DashboardServlet>
    dashboardServlet() {

    DashboardServlet servlet =
            new DashboardServlet();

    return new ServletRegistrationBean<>(
            servlet,
            "/dashboard"
    );
}


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
