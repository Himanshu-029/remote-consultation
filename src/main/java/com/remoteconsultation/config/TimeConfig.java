package com.remoteconsultation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Provides one application-wide Clock so that every "what time is it?"
 * decision uses the same time zone (property: app.timezone), regardless of
 * the JVM default or the MySQL server time zone.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock(
            @Value("${app.timezone:Asia/Kolkata}") String timeZone) {

        return Clock.system(ZoneId.of(timeZone));
    }
}
