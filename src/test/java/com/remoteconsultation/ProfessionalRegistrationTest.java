package com.remoteconsultation;

import com.remoteconsultation.dto.ProfessionalRegistrationRequest;
import com.remoteconsultation.model.User;
import com.remoteconsultation.service.ProfessionalService;
import com.remoteconsultation.dao.UserDao;
import com.remoteconsultation.dao.ProfessionalProfileDao;
import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProfessionalRegistrationTest {

    @Autowired
    private ProfessionalService professionalService;

    @Autowired
    private UserDao userDao;

    @Test
    void professionalRegistrationShouldCreateUserAndProfile()
            throws Exception {

        String uniqueId =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        String email =
                "professional.test."
                        + uniqueId
                        + "@example.com";

        String phone =
                "98"
                        + String.format(
                                "%08d",
                                System.nanoTime()
                                        % 100000000
                        );

        ProfessionalRegistrationRequest request =
                new ProfessionalRegistrationRequest();

        request.setFullName(
                "Test Healthcare Professional"
        );

        request.setEmail(email);

        request.setPassword(
                "Test@12345"
        );

        request.setPhone(phone);

        request.setSpecializationId(1L);

        request.setQualification(
                "MBBS"
        );

        request.setLicenseNumber(
                "TEST-LIC-" + uniqueId
        );

        request.setExperienceYears(5);

        request.setConsultationFee(
                "750.00"
        );

        request.setBio(
                "Test professional profile."
        );

        Long userId =
                professionalService.registerProfessional(
                        request
                );

        assertNotNull(userId);

        User user =
                userDao.findByEmail(email);

        assertNotNull(user);

        assertEquals(
                "PROFESSIONAL",
                user.getRole()
        );

        assertEquals(
                "ACTIVE",
                user.getStatus()
        );

        assertNotEquals(
                "Test@12345",
                user.getPasswordHash()
        );

        System.out.println(
                "Created professional user ID: "
                        + userId
        );
    }
}