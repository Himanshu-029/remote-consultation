package com.remoteconsultation.config;

import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.model.User;
import com.remoteconsultation.util.InputValidator;
import com.remoteconsultation.util.PasswordUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * Creates the first administrator account on startup.
 *
 * Nothing happens unless APP_ADMIN_EMAIL and APP_ADMIN_PASSWORD are set,
 * and an existing account with the same email is never modified.
 * No default credentials are stored in the repository.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserDao userDao;
    private final PasswordUtil passwordUtil;

    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public AdminBootstrap(
            UserDao userDao,
            PasswordUtil passwordUtil,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.admin.name:Administrator}") String adminName) {

        this.userDao = userDao;
        this.passwordUtil = passwordUtil;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }

        String email = adminEmail.trim();

        if (!InputValidator.isValidEmail(email)
                || !InputValidator.isValidPassword(adminPassword)) {
            logger.warn("Admin bootstrap skipped: APP_ADMIN_EMAIL must be a "
                    + "valid email and APP_ADMIN_PASSWORD must have at least "
                    + "8 characters (max 72 UTF-8 bytes).");
            return;
        }

        try {
            if (userDao.findByEmail(email) != null) {
                logger.info("Admin bootstrap skipped: an account with the "
                        + "configured email already exists.");
                return;
            }

            User admin = new User();
            admin.setFullName(
                    adminName.isBlank() ? "Administrator" : adminName.trim());
            admin.setEmail(email);
            admin.setPasswordHash(passwordUtil.hashPassword(adminPassword));
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");

            Long userId = userDao.createUser(admin);

            logger.info("Admin account created (user id {}).", userId);

        } catch (SQLException e) {
            logger.error("Admin bootstrap failed. Has database/schema.sql "
                    + "been applied?", e);
        }
    }
}
