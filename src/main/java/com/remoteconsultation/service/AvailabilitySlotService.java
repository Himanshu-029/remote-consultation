package com.remoteconsultation.service;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.AvailabilitySlotDao;
import com.remoteconsultation.dao.ProfessionalProfileDao;
import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.model.AvailabilitySlot;
import org.springframework.stereotype.Service;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AvailabilitySlotService {

    private final AvailabilitySlotDao availabilitySlotDao;
    private final ProfessionalProfileDao professionalProfileDao;
    private final UserDao userDao;
    private final JdbcTransactionManager transactionManager;

    public AvailabilitySlotService(
            AvailabilitySlotDao availabilitySlotDao,
            ProfessionalProfileDao professionalProfileDao,
            UserDao userDao,
            JdbcTransactionManager transactionManager) {

        this.availabilitySlotDao = availabilitySlotDao;
        this.professionalProfileDao = professionalProfileDao;
        this.userDao = userDao;
        this.transactionManager = transactionManager;
    }

    public Long createSlot(
            Long professionalId,
            LocalDate slotDate,
            LocalTime startTime,
            LocalTime endTime) throws SQLException {

        validateSlot(professionalId, slotDate, startTime, endTime);

        Connection connection = null;

        try {
            connection = transactionManager.getConnection();

            if (!userDao.isActiveProfessional(
                    connection, professionalId)) {
                throw new SecurityException(
                        "Only active professional accounts can create availability."
                );
            }

            String verificationStatus =
                    professionalProfileDao.findVerificationStatus(
                            connection, professionalId);

            if (!"APPROVED".equalsIgnoreCase(verificationStatus)) {
                throw new SecurityException(
                        "Your professional profile must be approved first."
                );
            }


            boolean overlapping =
                    availabilitySlotDao.hasOverlappingSlot(
                            connection,
                            professionalId,
                            slotDate,
                            startTime,
                            endTime
                    );

            if (overlapping) {
                throw new IllegalArgumentException(
                        "This availability overlaps an existing slot. "
                        + "Choose a different date or time."
                );
            }


            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setProfessionalId(professionalId);
            slot.setSlotDate(slotDate);
            slot.setStartTime(startTime);
            slot.setEndTime(endTime);
            slot.setStatus("AVAILABLE");

            Long slotId =
                    availabilitySlotDao.createSlot(connection, slot);

            transactionManager.commit(connection);

            return slotId;

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


public List<AvailabilitySlot> getAvailableSlots(
        Long professionalId) throws SQLException {

    if (professionalId == null || professionalId <= 0) {
        throw new IllegalArgumentException(
                "A valid professional ID is required.");
    }

    Connection connection = null;

    try {
        connection = transactionManager.getConnection();

        if (!userDao.isActiveProfessional(connection, professionalId)) {
            throw new IllegalArgumentException(
                    "This professional is not active.");
        }

        String verificationStatus =
                professionalProfileDao.findVerificationStatus(
                        connection, professionalId);

        if (!"APPROVED".equalsIgnoreCase(verificationStatus)) {
            throw new IllegalArgumentException(
                    "This professional is not approved.");
        }

        return availabilitySlotDao.findAvailableSlots(
                connection, professionalId);

    } finally {
        if (connection != null) {
            transactionManager.close(connection);
        }
    }
}




    private void validateSlot(
            Long professionalId,
            LocalDate slotDate,
            LocalTime startTime,
            LocalTime endTime) {

        if (professionalId == null || professionalId <= 0) {
            throw new IllegalArgumentException(
                    "A valid professional ID is required."
            );
        }

        if (slotDate == null || !slotDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Availability must be scheduled for a future date."
            );
        }

        if (startTime == null || endTime == null
                || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be later than start time."
            );
        }
    }
}
