
package com.remoteconsultation;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.AppointmentDao;
import com.remoteconsultation.dao.PatientProfileDao;
import com.remoteconsultation.model.Appointment;
import com.remoteconsultation.service.AppointmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.sql.PreparedStatement;
import org.mockito.ArgumentMatchers;


class AppointmentLifecycleTest {

    private AppointmentDao appointmentDao;
    private PatientProfileDao patientProfileDao;
    private JdbcTransactionManager transactionManager;
    private AppointmentService appointmentService;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        appointmentDao = mock(AppointmentDao.class);
        patientProfileDao = mock(PatientProfileDao.class);
        transactionManager = mock(JdbcTransactionManager.class);
        connection = mock(Connection.class);

        appointmentService = new AppointmentService(
                appointmentDao,
                patientProfileDao,
                transactionManager
        );

        when(transactionManager.getConnection())
                .thenReturn(connection);

        when(patientProfileDao.findPatientIdByUserId(connection, 79L))
                .thenReturn(10L);
    }

    @Test
    void shouldRejectCancellationOfCompletedAppointment()
            throws SQLException {

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(1L);
        appointment.setPatientId(10L);
        appointment.setSlotId(1L);
        appointment.setStatus("COMPLETED");

        when(appointmentDao.findPatientAppointmentForUpdate(
                connection, 1L, 10L))
                .thenReturn(appointment);

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.cancelPatientAppointment(
                        79L, 1L)
        );

        verify(appointmentDao, never())
                .cancelPatientAppointment(connection, 1L, 10L);

        verify(transactionManager).rollback(connection);
        verify(transactionManager).close(connection);
    }


    @Test
    void shouldCancelPendingAppointmentSuccessfully()
            throws SQLException {

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(2L);
        appointment.setPatientId(10L);
        appointment.setSlotId(3L);
        appointment.setStatus("PENDING");

        when(appointmentDao.findPatientAppointmentForUpdate(
                connection, 2L, 10L))
                .thenReturn(appointment);

        when(appointmentDao.cancelPatientAppointment(
                connection, 2L, 10L))
                .thenReturn(true);

        PreparedStatement releaseStatement = mock(PreparedStatement.class);

        when(connection.prepareStatement(ArgumentMatchers.anyString()))
            .thenReturn(releaseStatement);

        appointmentService.cancelPatientAppointment(79L, 2L);

        verify(appointmentDao).cancelPatientAppointment(
                connection, 2L, 10L);

        verify(transactionManager).commit(connection);
        verify(transactionManager, never()).rollback(connection);
        verify(transactionManager).close(connection);
    }


    @Test
    void shouldRejectCompletionOfPendingAppointment()
            throws SQLException {

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(3L);
        appointment.setProfessionalId(8L);
        appointment.setStatus("PENDING");

        when(appointmentDao.findAppointmentForUpdate(
                connection, 3L, 8L))
                .thenReturn(appointment);

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService
                        .completeConfirmedAppointment(8L, 3L)
        );

        verify(appointmentDao, never())
                .completeConfirmedAppointment(
                        connection, 3L, 8L);

        verify(transactionManager).rollback(connection);
        verify(transactionManager).close(connection);
    }


    @Test
    void shouldRejectCompletionBeforeScheduledEndTime()
            throws SQLException {

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(4L);
        appointment.setProfessionalId(8L);
        appointment.setSlotId(4L);
        appointment.setStatus("CONFIRMED");

        when(appointmentDao.findAppointmentForUpdate(
                connection, 4L, 8L))
                .thenReturn(appointment);

        java.sql.PreparedStatement scheduleStatement =
                mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet resultSet =
                mock(java.sql.ResultSet.class);

        when(connection.prepareStatement(
                org.mockito.ArgumentMatchers.contains(
                        "SELECT s.slot_date, s.end_time")))
                .thenReturn(scheduleStatement);

        when(scheduleStatement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);

        when(resultSet.getDate("slot_date"))
                .thenReturn(java.sql.Date.valueOf(
                        java.time.LocalDate.now(
                                java.time.ZoneId.of("Asia/Kolkata"))
                                .plusDays(2)));

        when(resultSet.getTime("end_time"))
                .thenReturn(java.sql.Time.valueOf(
                        java.time.LocalTime.of(23, 59)));

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService
                        .completeConfirmedAppointment(8L, 4L)
        );

        verify(appointmentDao, never())
                .completeConfirmedAppointment(
                        connection, 4L, 8L);

        verify(transactionManager).rollback(connection);
        verify(transactionManager).close(connection);
    }

}
