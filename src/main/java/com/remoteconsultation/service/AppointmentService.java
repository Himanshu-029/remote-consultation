
package com.remoteconsultation.service;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.AppointmentDao;
import com.remoteconsultation.dao.PatientProfileDao;
import com.remoteconsultation.model.Appointment;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
public class AppointmentService {

    private final AppointmentDao appointmentDao;
    private final PatientProfileDao patientProfileDao;
    private final JdbcTransactionManager transactionManager;

    public AppointmentService(
            AppointmentDao appointmentDao,
            PatientProfileDao patientProfileDao,
            JdbcTransactionManager transactionManager) {
        this.appointmentDao = appointmentDao;
        this.patientProfileDao = patientProfileDao;
        this.transactionManager = transactionManager;
    }

    public Long bookAppointment(
            Long loggedInUserId,
            Long slotId,
            String reason) throws SQLException {

        if (loggedInUserId == null || loggedInUserId <= 0) {
            throw new SecurityException(
                    "You must be logged in to book an appointment.");
        }

        if (slotId == null || slotId <= 0) {
            throw new IllegalArgumentException(
                    "A valid availability slot is required.");
        }

        if (reason != null && reason.trim().length() > 2000) {
            throw new IllegalArgumentException(
                    "The reason must not exceed 2000 characters.");
        }

        Connection connection = null;

        try {
            connection = transactionManager.getConnection();

            Long patientId =
                    patientProfileDao.findPatientIdByUserId(
                            connection, loggedInUserId);

            if (patientId == null) {
                throw new IllegalArgumentException(
                        "Your patient profile was not found.");
            }

            Appointment slot =
                    appointmentDao.lockAvailableSlot(
                            connection, slotId);

            if (slot == null) {
                throw new IllegalArgumentException(
                        "This slot is no longer available. "
                        + "Please choose another time.");
            }

            Long appointmentId =
                    appointmentDao.createAppointment(
                            connection,
                            slotId,
                            patientId,
                            slot.getProfessionalId(),
                            reason);

            appointmentDao.markSlotBooked(connection, slotId);

            transactionManager.commit(connection);

            return appointmentId;

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


public Long getProfessionalIdForAppointment(
        Long appointmentId) throws SQLException {

    if (appointmentId == null || appointmentId <= 0) {
        throw new IllegalArgumentException(
                "A valid appointment ID is required.");
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        Long professionalId =
                appointmentDao.findProfessionalIdByAppointmentId(
                        connection, appointmentId);

        transactionManager.commit(connection);
        return professionalId;

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


public Long getProfessionalIdForSlot(Long slotId)
        throws SQLException {

    if (slotId == null || slotId <= 0) {
        throw new IllegalArgumentException(
                "A valid slot ID is required.");
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        Long professionalId =
                appointmentDao.findProfessionalIdBySlotId(
                        connection, slotId);

        transactionManager.commit(connection);
        return professionalId;

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


public java.util.List<Appointment> getAppointmentsByPatientId(
        Long patientId) throws SQLException {

    if (patientId == null || patientId <= 0) {
        throw new IllegalArgumentException(
                "A valid patient ID is required.");
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        java.util.List<Appointment> appointments =
                appointmentDao.findAppointmentsByPatientId(
                        connection, patientId);

        transactionManager.commit(connection);

        return appointments;

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

public java.util.List<Appointment> getAppointmentsByProfessionalId(
        Long professionalId) throws SQLException {

    if (professionalId == null || professionalId <= 0) {
        throw new IllegalArgumentException(
                "A valid professional ID is required.");
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        java.util.List<Appointment> appointments =
                appointmentDao.findAppointmentsByProfessionalId(
                        connection, professionalId);

        transactionManager.commit(connection);
        return appointments;

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

public void updateAppointmentStatus(
        Long professionalId,
        Long appointmentId,
        String newStatus) throws SQLException {

    if (professionalId == null || professionalId <= 0
            || appointmentId == null || appointmentId <= 0) {
        throw new IllegalArgumentException(
                "Valid professional and appointment IDs are required.");
    }

    if (!"CONFIRMED".equals(newStatus)
            && !"REJECTED".equals(newStatus)) {
        throw new IllegalArgumentException(
                "Only CONFIRMED or REJECTED status is allowed.");
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        Appointment appointment =
                appointmentDao.findAppointmentForUpdate(
                        connection, appointmentId, professionalId);

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment not found for this professional.");
        }

        if (!"PENDING".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Only pending appointments can be updated.");
        }

        boolean updated =
                appointmentDao.updatePendingAppointmentStatus(
                        connection, appointmentId,
                        professionalId, newStatus);

        if (!updated) {
            throw new IllegalArgumentException(
                    "Appointment status has already changed. Refresh and try again.");
        }

        if ("REJECTED".equals(newStatus)) {
            String releaseSql = """
                    UPDATE availability_slots
                    SET status = 'AVAILABLE'
                    WHERE slot_id = ?
                      AND status = 'BOOKED'
                      AND (
                          slot_date > CURRENT_DATE
                          OR (
                              slot_date = CURRENT_DATE
                              AND start_time > CURRENT_TIME
                          )
                      )
                    """;

            try (java.sql.PreparedStatement statement =
                         connection.prepareStatement(releaseSql)) {
                statement.setLong(1, appointment.getSlotId());
                statement.executeUpdate();
            }
        }

        transactionManager.commit(connection);

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


public void cancelPatientAppointment(
        Long loggedInUserId,
        Long appointmentId) throws SQLException {

    if (loggedInUserId == null || loggedInUserId <= 0) {
        throw new SecurityException(
                "You must be logged in to cancel an appointment.");
    }

    if (appointmentId == null || appointmentId <= 0) {
        throw new IllegalArgumentException(
                "A valid appointment ID is required.");
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        Long patientId =
                patientProfileDao.findPatientIdByUserId(
                        connection, loggedInUserId);

        if (patientId == null) {
            throw new IllegalArgumentException(
                    "Your patient profile was not found.");
        }

        Appointment appointment =
                appointmentDao.findPatientAppointmentForUpdate(
                        connection, appointmentId, patientId);

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment not found for this patient.");
        }

        if (!"PENDING".equalsIgnoreCase(appointment.getStatus())
                && !"CONFIRMED".equalsIgnoreCase(
                        appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Only pending or confirmed appointments can be cancelled.");
        }

        boolean cancelled =
                appointmentDao.cancelPatientAppointment(
                        connection, appointmentId, patientId);

        if (!cancelled) {
            throw new IllegalArgumentException(
                    "The appointment status has changed. Refresh and try again.");
        }

        String releaseSql = """
                UPDATE availability_slots
                SET status = 'AVAILABLE'
                WHERE slot_id = ?
                  AND status = 'BOOKED'
                  AND (
                      slot_date > CURRENT_DATE
                      OR (
                          slot_date = CURRENT_DATE
                          AND start_time > CURRENT_TIME
                      )
                  )
                """;

        try (java.sql.PreparedStatement statement =
                     connection.prepareStatement(releaseSql)) {
            statement.setLong(1, appointment.getSlotId());
            statement.executeUpdate();
        }

        transactionManager.commit(connection);

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


public boolean completeConfirmedAppointment(
        Connection connection,
        Long appointmentId,
        Long professionalId) throws SQLException {

    String sql = """
            UPDATE appointments
            SET status = 'COMPLETED',
                completed_at = CURRENT_TIMESTAMP
            WHERE appointment_id = ?
              AND professional_id = ?
              AND status = 'CONFIRMED'
              AND EXISTS (
                  SELECT 1
                  FROM availability_slots s
                  WHERE s.slot_id = appointments.slot_id
                    AND TIMESTAMP(s.slot_date, s.end_time)
                        <= CURRENT_TIMESTAMP
              )
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, appointmentId);
        statement.setLong(2, professionalId);

        return statement.executeUpdate() == 1;
    }
}


public void completeConfirmedAppointment(
        Long professionalId,
        Long appointmentId) throws SQLException {

    if (professionalId == null || professionalId <= 0
            || appointmentId == null || appointmentId <= 0) {
        throw new IllegalArgumentException(
                "Valid professional and appointment IDs are required.");
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        Appointment appointment =
                appointmentDao.findAppointmentForUpdate(
                        connection, appointmentId, professionalId);

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment not found for this professional.");
        }

        if (!"CONFIRMED".equalsIgnoreCase(appointment.getStatus())) {
    throw new IllegalArgumentException(
            "Only confirmed appointments can be marked completed.");
}

String scheduledTimeSql = """
        SELECT s.slot_date, s.end_time
        FROM availability_slots s
        JOIN appointments a ON a.slot_id = s.slot_id
        WHERE a.appointment_id = ?
          AND a.professional_id = ?
        """;

try (java.sql.PreparedStatement statement =
             connection.prepareStatement(scheduledTimeSql)) {

    statement.setLong(1, appointmentId);
    statement.setLong(2, professionalId);

    try (java.sql.ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
            throw new IllegalArgumentException(
                    "Appointment schedule could not be found.");
        }

        java.time.LocalDate slotDate =
                rs.getDate("slot_date").toLocalDate();

        java.time.LocalTime endTime =
                rs.getTime("end_time").toLocalTime();

        java.time.LocalDateTime scheduledEnd =
                java.time.LocalDateTime.of(slotDate, endTime);

        java.time.LocalDateTime now =
                java.time.LocalDateTime.now(
                        java.time.ZoneId.of("Asia/Kolkata"));

        if (now.isBefore(scheduledEnd)) {
            throw new IllegalArgumentException(
                    "This appointment cannot be completed before its scheduled end time.");
        }
    }
}

        boolean completed =
                appointmentDao.completeConfirmedAppointment(
                        connection, appointmentId, professionalId);

        if (!completed) {
            throw new IllegalArgumentException(
                    "Appointment status has changed. Refresh and try again.");
        }

        transactionManager.commit(connection);

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


}
