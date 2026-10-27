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

        if (request.getSpecializationId() == null) {
            throw new IllegalArgumentException(
                    "Specialization is required."
            );
        }

        if (request.getQualification() == null
                || request.getQualification().isBlank()) {
            throw new IllegalArgumentException(
                    "Qualification is required."
            );
        }

        if (request.getLicenseNumber() == null
                || request.getLicenseNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "License number is required."
            );
        }

        if (request.getExperienceYears() == null
                || request.getExperienceYears() < 0) {
            throw new IllegalArgumentException(
                    "Experience years must be zero or greater."
            );
        }

        if (request.getConsultationFee() == null
                || request.getConsultationFee().isBlank()) {
            throw new IllegalArgumentException(
                    "Consultation fee is required."
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