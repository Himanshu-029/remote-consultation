package com.remoteconsultation.service;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.PatientProfileDao;
import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.dto.RegistrationRequest;
import com.remoteconsultation.model.PatientProfile;
import com.remoteconsultation.model.User;
import org.springframework.stereotype.Service;
import com.remoteconsultation.util.PasswordUtil;
import com.remoteconsultation.dto.LoginRequest;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class UserService {

    private final UserDao userDao;
    private final PatientProfileDao patientProfileDao;
    private final JdbcTransactionManager transactionManager;
    private final PasswordUtil passwordUtil;

    public UserService(UserDao userDao,
                   PatientProfileDao patientProfileDao,
                   JdbcTransactionManager transactionManager,
                   PasswordUtil passwordUtil) {

    this.userDao = userDao;
    this.patientProfileDao = patientProfileDao;
    this.transactionManager = transactionManager;
    this.passwordUtil = passwordUtil;
}
    public User findByEmail(String email) throws SQLException {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        return userDao.findByEmail(email.trim());
    }

    public Long registerPatient(RegistrationRequest request) throws SQLException {

        validatePatientRegistration(request);

        if (userDao.findByEmail(request.getEmail().trim()) != null) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(
        passwordUtil.hashPassword(request.getPassword())
);
        user.setPhone(request.getPhone().trim());
        user.setRole("PATIENT");
        user.setStatus("ACTIVE");

        PatientProfile patientProfile = new PatientProfile();

        patientProfile.setDateOfBirth(request.getDateOfBirth());
        patientProfile.setGender(request.getGender());
        patientProfile.setBloodGroup(request.getBloodGroup());
        patientProfile.setAddress(request.getAddress());
        patientProfile.setEmergencyContact(request.getEmergencyContact());

        Connection connection = null;

        try {
            connection = transactionManager.getConnection();

            Long userId = userDao.createUser(connection, user);

            patientProfileDao.createProfile(
                    connection,
                    userId,
                    patientProfile
            );

            transactionManager.commit(connection);

            user.setUserId(userId);

            return userId;

        } catch (SQLException | RuntimeException e) {

            if (connection != null) {
                transactionManager.rollback(connection);
            }

            throw e;

        } finally {

            if (connection != null) {
                transactionManager.close(connection);
            }
        }
    }

    private void validatePatientRegistration(
            RegistrationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Registration request cannot be null."
            );
        }

        if (request.getFullName() == null
                || request.getFullName().isBlank()) {

            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required."
            );
        }

        if (request.getPhone() == null
                || request.getPhone().isBlank()) {

            throw new IllegalArgumentException(
                    "Phone number is required."
            );
        }
    }
    public User login(LoginRequest request) throws SQLException {

    if (request == null) {
        throw new IllegalArgumentException(
                "Login request cannot be null."
        );
    }

    if (request.getEmail() == null
            || request.getEmail().isBlank()) {
        throw new IllegalArgumentException(
                "Email is required."
        );
    }

    if (request.getPassword() == null
            || request.getPassword().isBlank()) {
        throw new IllegalArgumentException(
                "Password is required."
        );
    }

    String email = request.getEmail().trim();

    User user = userDao.findByEmail(email);

    if (user == null) {
        throw new IllegalArgumentException(
                "Invalid email or password."
        );
    }

    if (!passwordUtil.verifyPassword(
            request.getPassword(),
            user.getPasswordHash())) {

        throw new IllegalArgumentException(
                "Invalid email or password."
        );
    }

    if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
        throw new IllegalArgumentException(
                "Your account is not active."
        );
    }

    return user;
}
}