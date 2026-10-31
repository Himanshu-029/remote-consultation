package com.remoteconsultation.dao;

import com.remoteconsultation.model.AvailabilitySlot;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class AvailabilitySlotDao {

        public Long createSlot(
                        Connection connection,
                        AvailabilitySlot slot) throws SQLException {

                String sql = """
                                INSERT INTO availability_slots
                                    (
                                        professional_id,
                                        slot_date,
                                        start_time,
                                        end_time,
                                        status
                                    )
                                VALUES (?, ?, ?, ?, 'AVAILABLE')
                                """;

                try (PreparedStatement statement = connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS)) {

                        statement.setLong(
                                        1, slot.getProfessionalId());

                        statement.setDate(
                                        2, java.sql.Date.valueOf(
                                                        slot.getSlotDate()));

                        statement.setTime(
                                        3, java.sql.Time.valueOf(
                                                        slot.getStartTime()));

                        statement.setTime(
                                        4, java.sql.Time.valueOf(
                                                        slot.getEndTime()));

                        int rowsAffected = statement.executeUpdate();

                        if (rowsAffected != 1) {
                                throw new SQLException(
                                                "Availability slot could not be created.");
                        }

                        try (ResultSet keys = statement.getGeneratedKeys()) {

                                if (keys.next()) {
                                        return keys.getLong(1);
                                }
                        }
                }

                throw new SQLException(
                                "Could not retrieve the availability slot ID.");
        }

        public boolean hasOverlappingSlot(
                        Connection connection,
                        Long professionalId,
                        java.time.LocalDate slotDate,
                        java.time.LocalTime startTime,
                        java.time.LocalTime endTime) throws SQLException {

                String sql = """
                                SELECT COUNT(*)
                                FROM availability_slots
                                WHERE professional_id = ?
                                  AND slot_date = ?
                                  AND status IN ('AVAILABLE', 'BOOKED')
                                  AND start_time < ?
                                  AND end_time > ?
                                """;

                try (PreparedStatement statement = connection.prepareStatement(sql)) {

                        statement.setLong(1, professionalId);
                        statement.setDate(
                                        2, java.sql.Date.valueOf(slotDate));
                        statement.setTime(
                                        3, java.sql.Time.valueOf(endTime));
                        statement.setTime(
                                        4, java.sql.Time.valueOf(startTime));

                        try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSet.next()
                                                && resultSet.getLong(1) > 0;
                        }
                }
        }

        public java.util.List<AvailabilitySlot> findAvailableSlots(
                        Connection connection,
                        Long professionalId) throws SQLException {

                String sql = """
                                SELECT slot_id,
                                       professional_id,
                                       slot_date,
                                       start_time,
                                       end_time,
                                       status
                                FROM availability_slots
                                WHERE professional_id = ?
                                  AND status = 'AVAILABLE'
                                  AND (
                                        slot_date > CURRENT_DATE
                                        OR (
                                            slot_date = CURRENT_DATE
                                            AND end_time > CURRENT_TIME
                                        )
                                  )
                                ORDER BY slot_date, start_time
                                """;

                java.util.List<AvailabilitySlot> slots = new java.util.ArrayList<>();

                try (PreparedStatement statement = connection.prepareStatement(sql)) {

                        statement.setLong(1, professionalId);

                        try (ResultSet resultSet = statement.executeQuery()) {

                                while (resultSet.next()) {
                                        AvailabilitySlot slot = new AvailabilitySlot();

                                        slot.setSlotId(resultSet.getLong("slot_id"));
                                        slot.setProfessionalId(
                                                        resultSet.getLong("professional_id"));
                                        slot.setSlotDate(
                                                        resultSet.getDate("slot_date").toLocalDate());
                                        slot.setStartTime(
                                                        resultSet.getTime("start_time").toLocalTime());
                                        slot.setEndTime(
                                                        resultSet.getTime("end_time").toLocalTime());
                                        slot.setStatus(resultSet.getString("status"));

                                        slots.add(slot);
                                }
                        }
                }

                return slots;
        }

}
