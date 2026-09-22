package com.remoteconsultation.dao;

import com.remoteconsultation.model.ProfessionalProfile;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}