
package com.remoteconsultation.service;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.ProfessionalProfileDao;
import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.dto.ProfessionalRegistrationRequest;
import com.remoteconsultation.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProfessionalServiceValidationTest {

    private UserDao userDao;
    private ProfessionalProfileDao profileDao;
    private JdbcTransactionManager transactionManager;
    private PasswordUtil passwordUtil;
    private ProfessionalService professionalService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        profileDao = mock(ProfessionalProfileDao.class);
        transactionManager = mock(JdbcTransactionManager.class);
        passwordUtil = mock(PasswordUtil.class);

        professionalService = new ProfessionalService(
                userDao,
                profileDao,
                transactionManager,
                passwordUtil
        );
    }

    @Test
    void rejectsNullRequest() {
        assertRejected(null);
    }

    @Test
    void rejectsInvalidEmail() {
        ProfessionalRegistrationRequest request = validRequest();
        request.setEmail("invalid-email");

        assertRejected(request);
    }

    @Test
    void rejectsShortPassword() {
        ProfessionalRegistrationRequest request = validRequest();
        request.setPassword("short");

        assertRejected(request);
    }

    @Test
    void rejectsInvalidPhone() {
        ProfessionalRegistrationRequest request = validRequest();
        request.setPhone("12345");

        assertRejected(request);
    }

    @Test
    void rejectsNonPositiveSpecializationId() {
        ProfessionalRegistrationRequest request = validRequest();
        request.setSpecializationId(0L);

        assertRejected(request);
    }

    @Test
    void rejectsExperienceAboveMaximum() {
        ProfessionalRegistrationRequest request = validRequest();
        request.setExperienceYears(81);

        assertRejected(request);
    }

    @Test
    void rejectsNegativeConsultationFee() {
        ProfessionalRegistrationRequest request = validRequest();
        request.setConsultationFee("-1");

        assertRejected(request);
    }

    @Test
    void rejectsConsultationFeeAboveMaximum() {
        ProfessionalRegistrationRequest request = validRequest();
        request.setConsultationFee("1000001");

        assertRejected(request);
    }

    @Test
    void rejectsMoreThanTwoFeeDecimalPlaces() {
        ProfessionalRegistrationRequest request = validRequest();
        request.setConsultationFee("250.123");

        assertRejected(request);
    }

    private void assertRejected(
            ProfessionalRegistrationRequest request) {

        assertThrows(
                IllegalArgumentException.class,
                () -> professionalService.registerProfessional(request)
        );

        verifyNoInteractions(
                userDao,
                profileDao,
                transactionManager,
                passwordUtil
        );
    }

    private ProfessionalRegistrationRequest validRequest() {
        ProfessionalRegistrationRequest request =
                new ProfessionalRegistrationRequest();

        request.setFullName("Test Professional");
        request.setEmail("professional@example.com");
        request.setPassword("SecurePass123");
        request.setPhone("9876543210");
        request.setSpecializationId(1L);
        request.setQualification("MBBS");
        request.setLicenseNumber("LICENSE-12345");
        request.setExperienceYears(5);
        request.setConsultationFee("500.00");
        request.setBio("Test professional biography");

        return request;
    }
}
