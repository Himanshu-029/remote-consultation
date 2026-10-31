
package com.remoteconsultation;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.AvailabilitySlotDao;
import com.remoteconsultation.dao.ProfessionalProfileDao;
import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.model.AvailabilitySlot;
import com.remoteconsultation.service.AvailabilitySlotService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvailabilitySlotServiceTest {

    private AvailabilitySlotDao availabilitySlotDao;
    private ProfessionalProfileDao professionalProfileDao;
    private UserDao userDao;
    private JdbcTransactionManager transactionManager;
    private AvailabilitySlotService service;
    private Connection connection;

    private final Long professionalId = 8L;
    private final LocalDate futureDate = LocalDate.now().plusDays(7);
    private final LocalTime start = LocalTime.of(10, 0);
    private final LocalTime end = LocalTime.of(11, 0);

    @BeforeEach
    void setUp() throws SQLException {
        availabilitySlotDao = mock(AvailabilitySlotDao.class);
        professionalProfileDao = mock(ProfessionalProfileDao.class);
        userDao = mock(UserDao.class);
        transactionManager = mock(JdbcTransactionManager.class);
        connection = mock(Connection.class);

        service = new AvailabilitySlotService(
                availabilitySlotDao,
                professionalProfileDao,
                userDao,
                transactionManager
        );

        when(transactionManager.getConnection()).thenReturn(connection);
        when(userDao.isActiveProfessional(connection, professionalId))
                .thenReturn(true);
        when(professionalProfileDao.lockProfessionalProfile(
                connection, professionalId))
                .thenReturn(true);
        when(professionalProfileDao.findVerificationStatus(
                connection, professionalId))
                .thenReturn("APPROVED");
    }

    @Test
    void shouldRejectOverlappingSlotAndRollback() throws SQLException {
        when(availabilitySlotDao.hasOverlappingSlot(
                connection, professionalId, futureDate, start, end))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createSlot(
                        professionalId, futureDate, start, end)
        );

        InOrder order = inOrder(
                professionalProfileDao, availabilitySlotDao);

        order.verify(professionalProfileDao)
                .lockProfessionalProfile(connection, professionalId);
        order.verify(availabilitySlotDao)
                .hasOverlappingSlot(
                        connection, professionalId, futureDate, start, end);

        verify(availabilitySlotDao, never())
                .createSlot(eq(connection), any(AvailabilitySlot.class));
        verify(transactionManager).rollback(connection);
        verify(transactionManager).close(connection);
        verify(transactionManager, never()).commit(connection);
    }

    @Test
    void shouldCreateAndCommitNonOverlappingSlot() throws SQLException {
        when(availabilitySlotDao.hasOverlappingSlot(
                connection, professionalId, futureDate, start, end))
                .thenReturn(false);

        when(availabilitySlotDao.createSlot(
                eq(connection), any(AvailabilitySlot.class)))
                .thenReturn(42L);

        Long slotId = service.createSlot(
                professionalId, futureDate, start, end);

        assertEquals(42L, slotId);

        verify(availabilitySlotDao).createSlot(
                eq(connection),
                argThat(slot ->
                        slot.getProfessionalId().equals(professionalId)
                        && slot.getSlotDate().equals(futureDate)
                        && slot.getStartTime().equals(start)
                        && slot.getEndTime().equals(end)
                        && "AVAILABLE".equals(slot.getStatus()))
        );

        verify(transactionManager).commit(connection);
        verify(transactionManager, never()).rollback(connection);
        verify(transactionManager).close(connection);
    }

    @Test
    void shouldRejectWhenProfessionalProfileCannotBeLocked()
            throws SQLException {
        when(professionalProfileDao.lockProfessionalProfile(
                connection, professionalId))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createSlot(
                        professionalId, futureDate, start, end)
        );

        verify(professionalProfileDao, never())
                .findVerificationStatus(connection, professionalId);
        verify(availabilitySlotDao, never())
                .hasOverlappingSlot(
                        any(), anyLong(), any(), any(), any());
        verify(availabilitySlotDao, never())
                .createSlot(any(), any());
        verify(transactionManager).rollback(connection);
        verify(transactionManager).close(connection);
    }
}
