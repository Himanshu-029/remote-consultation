
package com.remoteconsultation;

import com.remoteconsultation.dao.AvailabilitySlotDao;
import com.remoteconsultation.model.AvailabilitySlot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilitySlotDaoMySqlTest {

    private static Connection connection;
    private static AvailabilitySlotDao dao;
    private static long testProfessionalId;
    private static LocalDate testDate;

    @BeforeAll
    static void setUp() throws Exception {
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "DB_PASSWORD environment variable is not set.");
        }

        connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/remote_consultation"
                        + "?useSSL=false"
                        + "&serverTimezone=Asia/Kolkata"
                        + "&allowPublicKeyRetrieval=true",
                "root",
                password
        );

        connection.setAutoCommit(false);
        dao = new AvailabilitySlotDao();

        try {
            String unique = UUID.randomUUID()
                    .toString()
                    .replace("-", "");

            long userId;

            String insertUser = """
                    INSERT INTO users
                        (full_name, email, password_hash, phone, role, status)
                    VALUES (?, ?, ?, ?, 'PROFESSIONAL', 'ACTIVE')
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 insertUser,
                                 PreparedStatement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, "DAO Test Professional");
                statement.setString(
                        2, "dao-test-" + unique + "@example.invalid");
                statement.setString(3, "test-only-password-hash");
                statement.setString(4, "+1999" + unique.substring(0, 10));
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    assertTrue(keys.next(), "Test user ID should be generated.");
                    userId = keys.getLong(1);
                }
            }

            testProfessionalId = userId;

            String insertProfile = """
                    INSERT INTO professional_profiles
                        (professional_id, specialization_id, qualification,
                         license_number, experience_years, consultation_fee,
                         verification_status)
                    VALUES (?, 1, 'Test qualification', ?, 1.0, 100.00,
                            'APPROVED')
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(insertProfile)) {
                statement.setLong(1, testProfessionalId);
                statement.setString(2, "DAO-TEST-" + unique);
                statement.executeUpdate();
            }

            testDate = LocalDate.now().plusDays(30);

            // Insert one AVAILABLE slot: 10:00–11:00.
            insertSlot(
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    "AVAILABLE"
            );

            // Insert one BOOKED slot: 12:00–13:00.
            insertSlot(
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    "BOOKED"
            );

            connection.commit();

        } catch (Exception exception) {
            connection.rollback();
            connection.close();
            throw exception;
        }
    }

    private static void insertSlot(
            LocalTime start,
            LocalTime end,
            String status) throws SQLException {

        String sql = """
                INSERT INTO availability_slots
                    (professional_id, slot_date, start_time, end_time, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setLong(1, testProfessionalId);
            statement.setDate(2, java.sql.Date.valueOf(testDate));
            statement.setTime(3, java.sql.Time.valueOf(start));
            statement.setTime(4, java.sql.Time.valueOf(end));
            statement.setString(5, status);
            statement.executeUpdate();
        }
    }

    @Test
    void shouldDetectOverlapWithAvailableSlot() throws SQLException {
        assertTrue(dao.hasOverlappingSlot(
                connection,
                testProfessionalId,
                testDate,
                LocalTime.of(10, 15),
                LocalTime.of(10, 30)
        ));
    }

    @Test
    void shouldDetectOverlapWithBookedSlot() throws SQLException {
        assertTrue(dao.hasOverlappingSlot(
                connection,
                testProfessionalId,
                testDate,
                LocalTime.of(12, 15),
                LocalTime.of(12, 30)
        ));
    }

    @Test
    void shouldAllowAdjacentSlotBoundary() throws SQLException {
        assertFalse(dao.hasOverlappingSlot(
                connection,
                testProfessionalId,
                testDate,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        ));
    }

    @Test
    void shouldIgnoreSlotsOfAnotherProfessional() throws SQLException {
        // Use an ID that cannot match this test professional's slots.
        long otherProfessionalId = testProfessionalId + 1_000_000_000L;

        assertFalse(dao.hasOverlappingSlot(
                connection,
                otherProfessionalId,
                testDate,
                LocalTime.of(10, 15),
                LocalTime.of(10, 30)
        ));
    }

    @AfterAll
    static void cleanUp() throws Exception {
        if (connection == null || connection.isClosed()) {
            return;
        }

        try {
            connection.rollback();

            try (PreparedStatement deleteSlots =
                         connection.prepareStatement(
                                 "DELETE FROM availability_slots "
                                         + "WHERE professional_id = ?")) {
                deleteSlots.setLong(1, testProfessionalId);
                deleteSlots.executeUpdate();
            }

            try (PreparedStatement deleteProfile =
                         connection.prepareStatement(
                                 "DELETE FROM professional_profiles "
                                         + "WHERE professional_id = ?")) {
                deleteProfile.setLong(1, testProfessionalId);
                deleteProfile.executeUpdate();
            }

            try (PreparedStatement deleteUser =
                         connection.prepareStatement(
                                 "DELETE FROM users WHERE user_id = ?")) {
                deleteUser.setLong(1, testProfessionalId);
                deleteUser.executeUpdate();
            }

            connection.commit();

        } catch (Exception exception) {
            connection.rollback();
            throw exception;

        } finally {
            connection.close();
        }
    }
}
