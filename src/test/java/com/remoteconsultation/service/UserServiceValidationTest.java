
package com.remoteconsultation.service;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.PatientProfileDao;
import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.dto.RegistrationRequest;
import com.remoteconsultation.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceValidationTest {

    private UserDao userDao;
    private PatientProfileDao patientProfileDao;
    private JdbcTransactionManager transactionManager;
    private PasswordUtil passwordUtil;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        patientProfileDao = mock(PatientProfileDao.class);
        transactionManager = mock(JdbcTransactionManager.class);
        passwordUtil = mock(PasswordUtil.class);

        userService = new UserService(
                userDao,
                patientProfileDao,
                transactionManager,
                passwordUtil
        );
    }

    @Test
    void rejectsNullRegistrationRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerPatient(null)
        );

        verifyNoInteractions(
                userDao,
                patientProfileDao,
                transactionManager,
                passwordUtil
        );
    }

    @Test
    void rejectsInvalidEmailBeforeDatabaseAccess() {
        RegistrationRequest request = validRequest();
        request.setEmail("invalid-email");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerPatient(request)
        );

        verifyNoInteractions(
                userDao,
                patientProfileDao,
                transactionManager,
                passwordUtil
        );
    }

    @Test
    void rejectsShortPasswordBeforeDatabaseAccess() {
        RegistrationRequest request = validRequest();
        request.setPassword("short");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerPatient(request)
        );

        verifyNoInteractions(
                userDao,
                patientProfileDao,
                transactionManager,
                passwordUtil
        );
    }

    @Test
    void rejectsPasswordExceedingBcryptByteLimit() {
        RegistrationRequest request = validRequest();
        request.setPassword("a".repeat(73));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerPatient(request)
        );

        verifyNoInteractions(
                userDao,
                patientProfileDao,
                transactionManager,
                passwordUtil
        );
    }

    @Test
    void rejectsInvalidPhoneBeforeDatabaseAccess() {
        RegistrationRequest request = validRequest();
        request.setPhone("12345");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerPatient(request)
        );

        verifyNoInteractions(
                userDao,
                patientProfileDao,
                transactionManager,
                passwordUtil
        );
    }

    @Test
    void rejectsBlankFullNameBeforeDatabaseAccess() {
        RegistrationRequest request = validRequest();
        request.setFullName("   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerPatient(request)
        );

        verifyNoInteractions(
                userDao,
                patientProfileDao,
                transactionManager,
                passwordUtil
        );
    }

    private RegistrationRequest validRequest() {
        RegistrationRequest request = new RegistrationRequest();
        request.setFullName("Test Patient");
        request.setEmail("patient@example.com");
        request.setPassword("SecurePass123");
        request.setPhone("9876543210");
        return request;
    }
}
