
package com.remoteconsultation.dao;

import com.remoteconsultation.model.Appointment;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public class AppointmentDao {

    public Appointment lockAvailableSlot(
            Connection connection,
            Long slotId) throws SQLException {

        String sql = """
                SELECT slot_id,
                       professional_id,
                       slot_date,
                       start_time,
                       end_time,
                       status
                FROM availability_slots
                WHERE slot_id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, slotId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                if (!"AVAILABLE".equalsIgnoreCase(
                        resultSet.getString("status"))) {
                    return null;
                }

                LocalDate slotDate =
                        resultSet.getDate("slot_date").toLocalDate();
                LocalTime startTime =
                        resultSet.getTime("start_time").toLocalTime();

                if (slotDate.isBefore(LocalDate.now())
                        || (slotDate.equals(LocalDate.now())
                        && !startTime.isAfter(LocalTime.now()))) {
                    return null;
                }

                Appointment appointment = new Appointment();
                appointment.setSlotId(
                        resultSet.getLong("slot_id"));
                appointment.setProfessionalId(
                        resultSet.getLong("professional_id"));
                appointment.setSlotDate(slotDate);
                appointment.setStartTime(startTime);
                appointment.setEndTime(
                        resultSet.getTime("end_time").toLocalTime());

                return appointment;
            }
        }
    }


public Long createAppointment(
        Connection connection,
        Long slotId,
        Long patientId,
        Long professionalId,
        String reason) throws SQLException {

    String updateSql = """
            UPDATE appointments
            SET patient_id = ?,
                professional_id = ?,
                status = 'PENDING',
                reason = ?,
                booking_time = CURRENT_TIMESTAMP,
                cancelled_at = NULL,
                completed_at = NULL
            WHERE slot_id = ?
              AND status IN ('CANCELLED', 'REJECTED')
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(updateSql)) {

        statement.setLong(1, patientId);
        statement.setLong(2, professionalId);

        if (reason == null || reason.isBlank()) {
            statement.setNull(3, java.sql.Types.VARCHAR);
        } else {
            statement.setString(3, reason.trim());
        }

        statement.setLong(4, slotId);

        if (statement.executeUpdate() == 1) {
            String selectSql = """
                    SELECT appointment_id
                    FROM appointments
                    WHERE slot_id = ?
                    """;

            try (PreparedStatement selectStatement =
                         connection.prepareStatement(selectSql)) {

                selectStatement.setLong(1, slotId);

                try (ResultSet rs = selectStatement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("appointment_id");
                    }
                }
            }

            throw new SQLException(
                    "Could not retrieve the reused appointment ID.");
        }
    }

    String insertSql = """
            INSERT INTO appointments
                (slot_id, patient_id, professional_id, status, reason)
            VALUES (?, ?, ?, 'PENDING', ?)
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(
                         insertSql,
                         java.sql.Statement.RETURN_GENERATED_KEYS)) {

        statement.setLong(1, slotId);
        statement.setLong(2, patientId);
        statement.setLong(3, professionalId);

        if (reason == null || reason.isBlank()) {
            statement.setNull(4, java.sql.Types.VARCHAR);
        } else {
            statement.setString(4, reason.trim());
        }

        if (statement.executeUpdate() != 1) {
            throw new SQLException(
                    "Appointment could not be created.");
        }

        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getLong(1);
            }
        }
    }

    throw new SQLException("Could not retrieve the appointment ID.");
}


    public void markSlotBooked(
            Connection connection,
            Long slotId) throws SQLException {

        String sql = """
                UPDATE availability_slots
                SET status = 'BOOKED'
                WHERE slot_id = ?
                  AND status = 'AVAILABLE'
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, slotId);

            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "This slot is no longer available.");
            }
        }
    }


public Long findProfessionalIdByAppointmentId(
        Connection connection,
        Long appointmentId) throws SQLException {

    String sql = """
            SELECT professional_id
            FROM appointments
            WHERE appointment_id = ?
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, appointmentId);

        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong("professional_id");
            }
        }
    }

    return null;
}


public Long findProfessionalIdBySlotId(
        Connection connection,
        Long slotId) throws SQLException {

    String sql = """
            SELECT professional_id
            FROM availability_slots
            WHERE slot_id = ?
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, slotId);

        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong("professional_id");
            }
        }
    }

    return null;
}

public java.util.List<Appointment> findAppointmentsByPatientId(
        Connection connection,
        Long patientId) throws SQLException {

    String sql = """
            SELECT a.appointment_id,
                   a.slot_id,
                   a.patient_id,
                   a.professional_id,
                   a.status,
                   a.reason,
                   a.booking_time,
                   s.slot_date,
                   s.start_time,
                   s.end_time,
                   u.full_name AS professional_name,
                   sp.name AS specialization_name
            FROM appointments a
            JOIN availability_slots s
                ON s.slot_id = a.slot_id
            JOIN professional_profiles pp
                ON pp.professional_id = a.professional_id
            JOIN users u
                ON u.user_id = pp.professional_id
            LEFT JOIN specializations sp
                ON sp.specialization_id = pp.specialization_id
            WHERE a.patient_id = ?
            ORDER BY s.slot_date DESC, s.start_time DESC
            """;

    java.util.List<Appointment> appointments =
            new java.util.ArrayList<>();

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, patientId);

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Appointment appointment = new Appointment();

                appointment.setAppointmentId(
                        resultSet.getLong("appointment_id"));
                appointment.setSlotId(
                        resultSet.getLong("slot_id"));
                appointment.setPatientId(
                        resultSet.getLong("patient_id"));
                appointment.setProfessionalId(
                        resultSet.getLong("professional_id"));
                appointment.setStatus(
                        resultSet.getString("status"));
                appointment.setReason(
                        resultSet.getString("reason"));
                appointment.setSlotDate(
                        resultSet.getDate("slot_date").toLocalDate());
                appointment.setStartTime(
                        resultSet.getTime("start_time").toLocalTime());
                appointment.setEndTime(
                        resultSet.getTime("end_time").toLocalTime());
                appointment.setProfessionalName(
                        resultSet.getString("professional_name"));
                appointment.setSpecializationName(
                        resultSet.getString("specialization_name"));

                java.sql.Timestamp bookingTime =
                        resultSet.getTimestamp("booking_time");

                if (bookingTime != null) {
                    appointment.setBookingTime(
                            bookingTime.toLocalDateTime());
                }

                appointments.add(appointment);
            }
        }
    }

    return appointments;
}


public java.util.List<Appointment> findAppointmentsByProfessionalId(
        Connection connection,
        Long professionalId) throws SQLException {

    String sql = """
            SELECT a.appointment_id,
                   a.slot_id,
                   a.patient_id,
                   a.professional_id,
                   a.status,
                   a.reason,
                   a.booking_time,
                   s.slot_date,
                   s.start_time,
                   s.end_time,
                   u.full_name AS patient_name
            FROM appointments a
            JOIN availability_slots s
                ON s.slot_id = a.slot_id
            JOIN patient_profiles pp
                ON pp.patient_id = a.patient_id
            JOIN users u
                ON u.user_id = pp.patient_id
            WHERE a.professional_id = ?
            ORDER BY
                CASE WHEN a.status = 'PENDING' THEN 0 ELSE 1 END,
                s.slot_date ASC,
                s.start_time ASC
            """;

    java.util.List<Appointment> appointments =
            new java.util.ArrayList<>();

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, professionalId);

        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setAppointmentId(rs.getLong("appointment_id"));
                a.setSlotId(rs.getLong("slot_id"));
                a.setPatientId(rs.getLong("patient_id"));
                a.setProfessionalId(rs.getLong("professional_id"));
                a.setStatus(rs.getString("status"));
                a.setReason(rs.getString("reason"));
                a.setBookingTime(
                        rs.getTimestamp("booking_time").toLocalDateTime());
                a.setSlotDate(rs.getDate("slot_date").toLocalDate());
                a.setStartTime(rs.getTime("start_time").toLocalTime());
                a.setEndTime(rs.getTime("end_time").toLocalTime());
                a.setPatientName(rs.getString("patient_name"));
                appointments.add(a);
            }
        }
    }

    return appointments;
}

public Appointment findAppointmentForUpdate(
        Connection connection,
        Long appointmentId,
        Long professionalId) throws SQLException {

    String sql = """
            SELECT appointment_id, slot_id, professional_id, status
            FROM appointments
            WHERE appointment_id = ?
              AND professional_id = ?
            FOR UPDATE
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, appointmentId);
        statement.setLong(2, professionalId);

        try (ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                return null;
            }

            Appointment a = new Appointment();
            a.setAppointmentId(rs.getLong("appointment_id"));
            a.setSlotId(rs.getLong("slot_id"));
            a.setProfessionalId(rs.getLong("professional_id"));
            a.setStatus(rs.getString("status"));
            return a;
        }
    }
}

public boolean updatePendingAppointmentStatus(
        Connection connection,
        Long appointmentId,
        Long professionalId,
        String newStatus) throws SQLException {

    if (!"CONFIRMED".equals(newStatus)
            && !"REJECTED".equals(newStatus)) {
        throw new IllegalArgumentException("Invalid appointment status.");
    }

    String sql = """
            UPDATE appointments
            SET status = ?
            WHERE appointment_id = ?
              AND professional_id = ?
              AND status = 'PENDING'
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, newStatus);
        statement.setLong(2, appointmentId);
        statement.setLong(3, professionalId);

        return statement.executeUpdate() == 1;
    }
}


public Appointment findPatientAppointmentForUpdate(
        Connection connection,
        Long appointmentId,
        Long patientId) throws SQLException {

    String sql = """
            SELECT appointment_id,
                   slot_id,
                   patient_id,
                   professional_id,
                   status
            FROM appointments
            WHERE appointment_id = ?
              AND patient_id = ?
            FOR UPDATE
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, appointmentId);
        statement.setLong(2, patientId);

        try (ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                return null;
            }

            Appointment appointment = new Appointment();
            appointment.setAppointmentId(
                    rs.getLong("appointment_id"));
            appointment.setSlotId(
                    rs.getLong("slot_id"));
            appointment.setPatientId(
                    rs.getLong("patient_id"));
            appointment.setProfessionalId(
                    rs.getLong("professional_id"));
            appointment.setStatus(
                    rs.getString("status"));

            return appointment;
        }
    }
}

public boolean cancelPendingAppointment(
        Connection connection,
        Long appointmentId,
        Long patientId) throws SQLException {

    String sql = """
            UPDATE appointments
            SET status = 'CANCELLED',
                cancelled_at = CURRENT_TIMESTAMP
            WHERE appointment_id = ?
              AND patient_id = ?
              AND status = 'PENDING'
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, appointmentId);
        statement.setLong(2, patientId);

        return statement.executeUpdate() == 1;
    }
}

public boolean cancelPatientAppointment(
        Connection connection,
        Long appointmentId,
        Long patientId) throws SQLException {

    String sql = """
            UPDATE appointments
            SET status = 'CANCELLED',
                cancelled_at = CURRENT_TIMESTAMP
            WHERE appointment_id = ?
              AND patient_id = ?
              AND status IN ('PENDING', 'CONFIRMED')
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, appointmentId);
        statement.setLong(2, patientId);

        return statement.executeUpdate() == 1;
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
            """;

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setLong(1, appointmentId);
        statement.setLong(2, professionalId);

        return statement.executeUpdate() == 1;
    }
}


}
