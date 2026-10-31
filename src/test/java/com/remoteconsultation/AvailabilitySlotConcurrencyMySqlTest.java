
package com.remoteconsultation;

import com.remoteconsultation.config.DatabaseConnection;
import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.AvailabilitySlotDao;
import com.remoteconsultation.dao.ProfessionalProfileDao;
import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.service.AvailabilitySlotService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilitySlotConcurrencyMySqlTest {

    private static HikariDataSource dataSource;
    private static AvailabilitySlotService service;
    private static long professionalId;
    private static LocalDate testDate;
    private static String testEmail;
    private static String testLicense;

    @BeforeAll
    static void setUp() throws Exception {
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "DB_PASSWORD environment variable is not set.");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://localhost:3306/remote_consultation"
                        + "?useSSL=false"
                        + "&serverTimezone=Asia/Kolkata"
                        + "&allowPublicKeyRetrieval=true");
        config.setUsername("root");
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);

        dataSource = new HikariDataSource(config);

        DatabaseConnection databaseConnection =
                new DatabaseConnection(dataSource);

        UserDao userDao = new UserDao(databaseConnection);
        ProfessionalProfileDao profileDao =
                new ProfessionalProfileDao();
        AvailabilitySlotDao slotDao = new AvailabilitySlotDao();
        JdbcTransactionManager transactionManager =
                new JdbcTransactionManager(dataSource);

        service = new AvailabilitySlotService(
                slotDao, profileDao, userDao, transactionManager);

        String unique = UUID.randomUUID()
                .toString().replace("-", "");
        testEmail = "concurrency-test-" + unique + "@example.invalid";
        testLicense = "CONCURRENCY-" + unique;

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String insertUser = """
                        INSERT INTO users
                            (full_name, email, password_hash, role, status)
                        VALUES (?, ?, ?, 'PROFESSIONAL', 'ACTIVE')
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     insertUser,
                                     PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, "Concurrency Test Professional");
                    statement.setString(2, testEmail);
                    statement.setString(3, "test-only-password-hash");
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        assertTrue(keys.next());
                        professionalId = keys.getLong(1);
                    }
                }

                String insertProfile = """
                        INSERT INTO professional_profiles
                            (professional_id, specialization_id,
                             qualification, license_number,
                             experience_years, consultation_fee,
                             verification_status)
                        VALUES (?, 1, 'Test qualification', ?,
                                1.0, 100.00, 'APPROVED')
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(insertProfile)) {
                    statement.setLong(1, professionalId);
                    statement.setString(2, testLicense);
                    statement.executeUpdate();
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }

        testDate = LocalDate.now().plusDays(30);
    }

    @Test
    void concurrentOverlappingRequestsMustNotBothSucceed()
            throws Exception {

        int threadCount = 2;
        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Boolean>> results = new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                final int requestNumber = i;

                results.add(executor.submit(() -> {
                    ready.countDown();

                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Timed out waiting for concurrent start.");
                    }

                    LocalTime startTime = requestNumber == 0
                            ? LocalTime.of(10, 0)
                            : LocalTime.of(10, 15);

                    LocalTime endTime = requestNumber == 0
                            ? LocalTime.of(11, 0)
                            : LocalTime.of(11, 15);

                    try {
                        service.createSlot(
                                professionalId,
                                testDate,
                                startTime,
                                endTime);
                        return true;
                    } catch (IllegalArgumentException expectedOverlap) {
                        return false;
                    }
                }));
            }

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both requests should be ready.");

            start.countDown();

            int successes = 0;
            int rejected = 0;

            for (Future<Boolean> result : results) {
                if (result.get(20, TimeUnit.SECONDS)) {
                    successes++;
                } else {
                    rejected++;
                }
            }

            assertEquals(
                    1, successes,
                    "Exactly one overlapping slot request should succeed.");
            assertEquals(
                    1, rejected,
                    "Exactly one overlapping slot request should be rejected.");

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         SELECT COUNT(*)
                         FROM availability_slots
                         WHERE professional_id = ?
                           AND slot_date = ?
                           AND status IN ('AVAILABLE', 'BOOKED')
                           AND start_time < '11:15:00'
                           AND end_time > '10:00:00'
                         """)) {

                statement.setLong(1, professionalId);
                statement.setDate(
                        2, java.sql.Date.valueOf(testDate));

                try (ResultSet rs = statement.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(
                            1, rs.getInt(1),
                            "Only one slot should exist in the overlapping window.");
                }
            }
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(
                    executor.awaitTermination(10, TimeUnit.SECONDS),
                    "Test threads should terminate.");
        }
    }

    @AfterAll
    static void cleanUp() throws Exception {
        if (dataSource == null) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "DELETE FROM availability_slots "
                                             + "WHERE professional_id = ?")) {
                    statement.setLong(1, professionalId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "DELETE FROM professional_profiles "
                                             + "WHERE professional_id = ?")) {
                    statement.setLong(1, professionalId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "DELETE FROM users WHERE user_id = ?")) {
                    statement.setLong(1, professionalId);
                    statement.executeUpdate();
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } finally {
            dataSource.close();
        }
    }
}
