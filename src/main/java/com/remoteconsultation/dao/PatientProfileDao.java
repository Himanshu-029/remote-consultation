package com.remoteconsultation.dao;

import com.remoteconsultation.model.PatientProfile;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class PatientProfileDao {

    // Create patient profile using an existing transaction connection
    public void createProfile(Connection connection,
                              Long patientId,
                              PatientProfile profile) throws SQLException {

        String sql = """
                INSERT INTO patient_profiles
                    (patient_id,
                     date_of_birth,
                     gender,
                     blood_group,
                     address,
                     emergency_contact)
                VALUES
                    (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, patientId);

            if (profile.getDateOfBirth() != null) {
                statement.setDate(
                        2,
                        java.sql.Date.valueOf(profile.getDateOfBirth())
                );
            } else {
                statement.setNull(
                        2,
                        java.sql.Types.DATE
                );
            }

            statement.setString(3, profile.getGender());
            statement.setString(4, profile.getBloodGroup());
            statement.setString(5, profile.getAddress());
            statement.setString(6, profile.getEmergencyContact());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Patient profile creation failed.");
            }
        }
    }


    public Long findPatientIdByUserId(
            Connection connection,
            Long userId) throws SQLException {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }

        String sql = """
                SELECT patient_id
                FROM patient_profiles
                WHERE patient_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (java.sql.ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong("patient_id");
                }
            }
        }

        return null;
    }

}