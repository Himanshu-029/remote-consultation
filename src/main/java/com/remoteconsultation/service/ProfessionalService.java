package com.remoteconsultation.service;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.ProfessionalProfileDao;
import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.dto.ProfessionalRegistrationRequest;
import com.remoteconsultation.model.ProfessionalProfile;
import com.remoteconsultation.model.User;
import com.remoteconsultation.util.PasswordUtil;
import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import com.remoteconsultation.model.PendingProfessionalReview;
import com.remoteconsultation.util.InputValidator;


@Service
public class ProfessionalService {

    private final UserDao userDao;
    private final ProfessionalProfileDao professionalProfileDao;
    private final JdbcTransactionManager transactionManager;
    private final PasswordUtil passwordUtil;

    public ProfessionalService(
            UserDao userDao,
            ProfessionalProfileDao professionalProfileDao,
            JdbcTransactionManager transactionManager,
            PasswordUtil passwordUtil) {

        this.userDao = userDao;
        this.professionalProfileDao = professionalProfileDao;
        this.transactionManager = transactionManager;
        this.passwordUtil = passwordUtil;
    }

    public Long registerProfessional(
            ProfessionalRegistrationRequest request)
            throws SQLException {

        validateRegistration(request);

        String email = request.getEmail().trim();

        if (userDao.findByEmail(email) != null) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        BigDecimal consultationFee;

        try {
            consultationFee =
                    new BigDecimal(
                            request.getConsultationFee().trim()
                    );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Consultation fee must be a valid number."
            );
        }

        if (consultationFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Consultation fee cannot be negative."
            );
        }

        User user = new User();

        user.setFullName(
                request.getFullName().trim()
        );

        user.setEmail(email);

        user.setPasswordHash(
                passwordUtil.hashPassword(
                        request.getPassword()
                )
        );

        user.setPhone(
                request.getPhone().trim()
        );

        /*
         * Role is controlled by the service.
         * Never trust the registration request for this value.
         */
        user.setRole("PROFESSIONAL");

        user.setStatus("ACTIVE");

        ProfessionalProfile profile =
                new ProfessionalProfile();

        profile.setSpecializationId(
                request.getSpecializationId()
        );

        profile.setQualification(
                request.getQualification().trim()
        );

        profile.setLicenseNumber(
                request.getLicenseNumber().trim()
        );

        profile.setExperienceYears(
                request.getExperienceYears()
        );

        profile.setConsultationFee(
                consultationFee
        );

        profile.setBio(
                request.getBio()
        );

        /*
         * Newly registered professionals must be
         * verified by an administrator before booking.
         */
        profile.setVerificationStatus("PENDING");

        Connection connection = null;

        try {

            connection =
                    transactionManager.getConnection();

            Long userId =
                    userDao.createUser(
                            connection,
                            user
                    );

            professionalProfileDao.createProfile(
                    connection,
                    userId,
                    profile
            );

            transactionManager.commit(connection);

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

    public String getProfessionalVerificationStatus(
        Long professionalId)
        throws SQLException {

    if (professionalId == null) {
        throw new IllegalArgumentException(
                "Professional ID is required."
        );
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        String status =
                professionalProfileDao.findVerificationStatus(
                        connection,
                        professionalId
                );

        if (status == null) {
            throw new SQLException(
                    "Professional profile was not found."
            );
        }

        return status;

    } finally {
        if (connection != null) {
            transactionManager.close(connection);
        }
    }
}


public void requireApprovedProfessional(Long professionalId)
        throws SQLException {

    String status = getProfessionalVerificationStatus(professionalId);

    if (!"APPROVED".equalsIgnoreCase(status)) {
        throw new SecurityException(
                "Your professional account must be approved "
                + "by an administrator before you can perform "
                + "this action."
        );
    }
}



private void validateRegistration(
        ProfessionalRegistrationRequest request) {

    if (request == null) {
        throw new IllegalArgumentException(
                "Registration request cannot be null."
        );
    }

    if (!InputValidator.isNotBlank(request.getFullName())) {
        throw new IllegalArgumentException(
                "Full name is required."
        );
    }

    if (!InputValidator.isValidEmail(request.getEmail())) {
        throw new IllegalArgumentException(
                "Please provide a valid email address."
        );
    }

    if (!InputValidator.isValidPassword(request.getPassword())) {
        throw new IllegalArgumentException(
                "Password must contain at least 8 characters and no more than 72 UTF-8 bytes."
        );
    }

    if (!InputValidator.isValidPhone(request.getPhone())) {
        throw new IllegalArgumentException(
                "Please provide a valid phone number containing 10 to 15 digits, optionally starting with +."
        );
    }

    if (request.getSpecializationId() == null
            || request.getSpecializationId() <= 0) {
        throw new IllegalArgumentException(
                "Please select a valid specialization."
        );
    }

    if (!InputValidator.isNotBlank(request.getQualification())) {
        throw new IllegalArgumentException(
                "Qualification is required."
        );
    }

    if (!InputValidator.isNotBlank(request.getLicenseNumber())) {
        throw new IllegalArgumentException(
                "License number is required."
        );
    }

    if (request.getExperienceYears() == null
            || request.getExperienceYears() < 0
            || request.getExperienceYears() > 80) {
        throw new IllegalArgumentException(
                "Experience years must be between 0 and 80."
        );
    }

    if (!InputValidator.isNotBlank(request.getConsultationFee())) {
        throw new IllegalArgumentException(
                "Consultation fee is required."
        );
    }

    final BigDecimal fee;

    try {
        fee = new BigDecimal(request.getConsultationFee().trim());
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
                "Consultation fee must be a valid number."
        );
    }

    if (fee.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException(
                "Consultation fee cannot be negative."
        );
    }

    if (fee.compareTo(new BigDecimal("1000000")) > 0) {
        throw new IllegalArgumentException(
                "Consultation fee cannot exceed 1000000."
        );
    }

    if (fee.scale() > 2) {
        throw new IllegalArgumentException(
                "Consultation fee can have at most two decimal places."
        );
    }
}


    public List<ProfessionalProfile> getPendingProfessionals()
        throws SQLException {

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        return professionalProfileDao.findPendingProfessionals(
                connection
        );

    } finally {
        if (connection != null) {
            transactionManager.close(connection);
        }
    }
}
public boolean reviewProfessional(
        Long professionalId,
        String decision) throws SQLException {

    if (professionalId == null) {
        throw new IllegalArgumentException(
                "Professional ID is required."
        );
    }

    if (decision == null) {
        throw new IllegalArgumentException(
                "A verification decision is required."
        );
    }

    String normalizedDecision =
            decision.trim().toUpperCase(java.util.Locale.ROOT);

    if (!"APPROVED".equals(normalizedDecision)
            && !"REJECTED".equals(normalizedDecision)) {
        throw new IllegalArgumentException(
                "Decision must be APPROVED or REJECTED."
        );
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        boolean updated =
                professionalProfileDao.updateVerificationStatus(
                        connection,
                        professionalId,
                        normalizedDecision
                );

        if (updated) {
            transactionManager.commit(connection);
        } else {
            transactionManager.rollback(connection);
        }

        return updated;

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


public List<PendingProfessionalReview> getPendingProfessionalReviews()
        throws SQLException {

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        return professionalProfileDao.findPendingProfessionalReviews(
                connection
        );
    } finally {
        transactionManager.close(connection);
    }
}



}