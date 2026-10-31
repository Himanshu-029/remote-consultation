package com.remoteconsultation;

import com.remoteconsultation.dto.RegistrationRequest;
import com.remoteconsultation.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PatientRegistrationTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldRegisterPatientSuccessfully() throws Exception {

        RegistrationRequest request = new RegistrationRequest();

        request.setFullName("Test Patient");
        request.setEmail("test.patient." + System.currentTimeMillis() + "@example.com");
        request.setPassword("Test@12345");
        request.setPhone(
        String.format(
                "98%08d",
                System.currentTimeMillis() % 100000000L
            )
        );

        request.setDateOfBirth(
                java.time.LocalDate.of(2002, 5, 15)
        );

        request.setGender("Male");
        request.setBloodGroup("O+");
        request.setAddress("Test Address");
        request.setEmergencyContact("9876543210");

        Long userId = userService.registerPatient(request);

        assertNotNull(userId);
        assertTrue(userId > 0);

        System.out.println(
                "Patient registered successfully. User ID: " + userId
        );
    }
}