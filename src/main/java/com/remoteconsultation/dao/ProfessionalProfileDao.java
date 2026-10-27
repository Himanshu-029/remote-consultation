package com.remoteconsultation.dao;

import com.remoteconsultation.model.ProfessionalProfile;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.remoteconsultation.model.PendingProfessionalReview;
import com.remoteconsultation.model.ProfessionalDirectoryItem;
import java.math.BigDecimal;

@Repository
public class ProfessionalProfileDao {

    public void createProfile(
            Connection connection,
            Long professionalId,
            ProfessionalProfile profile)
            throws SQLException {

        String sql = """
                INSERT INTO professional_profiles
                    (
                        professional_id,
                        specialization_id,
                        qualification,
                        license_number,
                        experience_years,
                        consultation_fee,
                        bio,
                        verification_status
                    )
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    professionalId
            );

            statement.setLong(
                    2,
                    profile.getSpecializationId()
            );

            statement.setString(
                    3,
                    profile.getQualification()
            );

            statement.setString(
                    4,
                    profile.getLicenseNumber()
            );

            if (profile.getExperienceYears() != null) {
                statement.setInt(
                        5,
                        profile.getExperienceYears()
                );
            } else {
                statement.setNull(
                        5,
                        java.sql.Types.INTEGER
                );
            }

            if (profile.getConsultationFee() != null) {
                statement.setBigDecimal(
                        6,
                        profile.getConsultationFee()
                );
            } else {
                statement.setNull(
                        6,
                        java.sql.Types.DECIMAL
                );
            }

            statement.setString(
                    7,
                    profile.getBio()
            );

            statement.setString(
                    8,
                    profile.getVerificationStatus()
            );

            int rowsAffected =
                    statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException(
                        "Professional profile creation failed."
                );
            }
        }
    }

    public String findVerificationStatus(
        Connection connection,
        Long professionalId)
        throws SQLException {

    String sql = """
            SELECT verification_status
            FROM professional_profiles
            WHERE professional_id = ?
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, professionalId);

        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString("verification_status");
            }
        }
    }

    return null;
}

public List<ProfessionalProfile> findPendingProfessionals(
        Connection connection) throws SQLException {

    String sql = """
            SELECT
                pp.professional_id,
                pp.specialization_id,
                pp.qualification,
                pp.license_number,
                pp.experience_years,
                pp.consultation_fee,
                pp.bio,
                pp.verification_status,
                pp.created_at,
                pp.updated_at
            FROM professional_profiles pp
            INNER JOIN users u
                ON u.user_id = pp.professional_id
            WHERE pp.verification_status = ?
              AND u.role = ?
            ORDER BY pp.created_at ASC
            """;

    List<ProfessionalProfile> professionals = new ArrayList<>();

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, "PENDING");
        statement.setString(2, "PROFESSIONAL");

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ProfessionalProfile profile = new ProfessionalProfile();

                profile.setProfessionalId(
                        resultSet.getLong("professional_id"));
                profile.setSpecializationId(
                        resultSet.getLong("specialization_id"));
                profile.setQualification(
                        resultSet.getString("qualification"));
                profile.setLicenseNumber(
                        resultSet.getString("license_number"));
                profile.setExperienceYears(
                        resultSet.getInt("experience_years"));
                profile.setConsultationFee(
                        resultSet.getBigDecimal("consultation_fee"));
                profile.setBio(
                        resultSet.getString("bio"));
                profile.setVerificationStatus(
                        resultSet.getString("verification_status"));

                if (resultSet.getTimestamp("created_at") != null) {
                    profile.setCreatedAt(
                            resultSet.getTimestamp("created_at")
                                    .toLocalDateTime());
                }

                if (resultSet.getTimestamp("updated_at") != null) {
                    profile.setUpdatedAt(
                            resultSet.getTimestamp("updated_at")
                                    .toLocalDateTime());
                }

                professionals.add(profile);
            }
        }
    }

    return professionals;
}


public boolean updateVerificationStatus(
        Connection connection,
        Long professionalId,
        String newStatus) throws SQLException {

    if (professionalId == null) {
        throw new IllegalArgumentException(
                "Professional ID is required."
        );
    }

    if (!"APPROVED".equals(newStatus)
            && !"REJECTED".equals(newStatus)) {
        throw new IllegalArgumentException(
                "Verification status must be APPROVED or REJECTED."
        );
    }

    String sql = """
            UPDATE professional_profiles pp
            INNER JOIN users u
                ON u.user_id = pp.professional_id
            SET pp.verification_status = ?
            WHERE pp.professional_id = ?
              AND pp.verification_status = 'PENDING'
              AND u.role = 'PROFESSIONAL'
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, newStatus);
        statement.setLong(2, professionalId);

        return statement.executeUpdate() == 1;
    }
}



public List<PendingProfessionalReview> findPendingProfessionalReviews(
        Connection connection) throws SQLException {

    String sql = """
            SELECT
                u.user_id,
                u.full_name,
                u.email,
                s.name AS specialization_name,
                pp.qualification,
                pp.license_number,
                pp.experience_years,
                pp.consultation_fee,
                pp.bio,
                pp.verification_status,
                pp.created_at
            FROM professional_profiles pp
            INNER JOIN users u
                ON u.user_id = pp.professional_id
            INNER JOIN specializations s
                ON s.specialization_id = pp.specialization_id
            WHERE pp.verification_status = ?
              AND u.role = ?
            ORDER BY pp.created_at ASC
            """;

    List<PendingProfessionalReview> reviews = new ArrayList<>();

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, "PENDING");
        statement.setString(2, "PROFESSIONAL");

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                PendingProfessionalReview review =
                        new PendingProfessionalReview();

                review.setProfessionalId(
                        resultSet.getLong("user_id"));
                review.setFullName(
                        resultSet.getString("full_name"));
                review.setEmail(
                        resultSet.getString("email"));
                review.setSpecializationName(
                        resultSet.getString("specialization_name"));
                review.setQualification(
                        resultSet.getString("qualification"));
                review.setLicenseNumber(
                        resultSet.getString("license_number"));
                review.setExperienceYears(
                        resultSet.getBigDecimal("experience_years"));
                review.setConsultationFee(
                        resultSet.getBigDecimal("consultation_fee"));
                review.setBio(
                        resultSet.getString("bio"));
                review.setVerificationStatus(
                        resultSet.getString("verification_status"));

                java.sql.Timestamp createdAt =
                        resultSet.getTimestamp("created_at");

                if (createdAt != null) {
                    review.setCreatedAt(
                            createdAt.toLocalDateTime());
                }

                reviews.add(review);
            }
        }
    }

    return reviews;
}


public List<ProfessionalDirectoryItem> findApprovedProfessionals(
        Connection connection) throws SQLException {

    String sql = """
            SELECT
                u.user_id AS professional_id,
                u.full_name,
                s.name AS specialization_name,
                pp.qualification,
                pp.experience_years,
                pp.consultation_fee,
                pp.bio
            FROM professional_profiles pp
            INNER JOIN users u
                ON u.user_id = pp.professional_id
            INNER JOIN specializations s
                ON s.specialization_id = pp.specialization_id
            WHERE u.role = 'PROFESSIONAL'
              AND u.status = 'ACTIVE'
              AND pp.verification_status = 'APPROVED'
            ORDER BY u.full_name ASC
            """;

    List<ProfessionalDirectoryItem> professionals =
            new ArrayList<>();

    try (PreparedStatement statement =
                 connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {
            ProfessionalDirectoryItem item =
                    new ProfessionalDirectoryItem();

            item.setProfessionalId(
                    resultSet.getLong("professional_id"));

            item.setFullName(
                    resultSet.getString("full_name"));

            item.setSpecializationName(
                    resultSet.getString("specialization_name"));

            item.setQualification(
                    resultSet.getString("qualification"));

            item.setExperienceYears(
                    resultSet.getBigDecimal("experience_years"));

            item.setConsultationFee(
                    resultSet.getBigDecimal("consultation_fee"));

            item.setBio(
                    resultSet.getString("bio"));

            professionals.add(item);
        }
    }

    return professionals;
}


}