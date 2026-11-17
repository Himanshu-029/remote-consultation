-- =====================================================================
-- Remote Consultation - MySQL 8 schema
--
-- Usage:   mysql -u root -p < database/schema.sql
--
-- Reverse-engineered from the DAO queries and tests. The script is
-- idempotent: re-running it never drops or overwrites existing data.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS remote_consultation
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE remote_consultation;

-- ---------------------------------------------------------------------
-- users: every account (patient, professional, admin)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id        BIGINT       NOT NULL AUTO_INCREMENT,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(254) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,          -- BCrypt (60 chars)
    phone          VARCHAR(20)  NULL,
    role           ENUM('PATIENT', 'PROFESSIONAL', 'ADMIN') NOT NULL,
    status         ENUM('ACTIVE', 'INACTIVE')      NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)              -- case-insensitive collation
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- patient_profiles: one row per patient (shares the users primary key)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient_profiles (
    patient_id         BIGINT       NOT NULL,
    date_of_birth      DATE         NULL,
    gender             VARCHAR(20)  NULL,
    blood_group        VARCHAR(5)   NULL,
    address            VARCHAR(500) NULL,
    emergency_contact  VARCHAR(100) NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (patient_id),
    CONSTRAINT fk_patient_user
        FOREIGN KEY (patient_id) REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- specializations: lookup table.
-- IDs 1-6 must match the options in professional-register.jsp.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS specializations (
    specialization_id  INT          NOT NULL AUTO_INCREMENT,
    name               VARCHAR(100) NOT NULL,
    PRIMARY KEY (specialization_id),
    UNIQUE KEY uq_specializations_name (name)
) ENGINE = InnoDB;

INSERT IGNORE INTO specializations (specialization_id, name) VALUES
    (1, 'General Medicine'),
    (2, 'Cardiology'),
    (3, 'Dermatology'),
    (4, 'Neurology'),
    (5, 'Pediatrics'),
    (6, 'Orthopedics');

-- ---------------------------------------------------------------------
-- professional_profiles: doctors/specialists; must be approved by an
-- admin (verification_status) before they can publish availability.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS professional_profiles (
    professional_id     BIGINT        NOT NULL,
    specialization_id   INT           NOT NULL,
    qualification       VARCHAR(255)  NOT NULL,
    license_number      VARCHAR(100)  NOT NULL,
    experience_years    DECIMAL(4, 1) NULL,
    consultation_fee    DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    bio                 TEXT          NULL,
    verification_status ENUM('PENDING', 'APPROVED', 'REJECTED')
                                      NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (professional_id),
    UNIQUE KEY uq_professional_license (license_number),
    KEY idx_professional_verification (verification_status),
    CONSTRAINT fk_professional_user
        FOREIGN KEY (professional_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_professional_specialization
        FOREIGN KEY (specialization_id)
        REFERENCES specializations (specialization_id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- availability_slots: bookable time windows published by professionals
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS availability_slots (
    slot_id          BIGINT NOT NULL AUTO_INCREMENT,
    professional_id  BIGINT NOT NULL,
    slot_date        DATE   NOT NULL,
    start_time       TIME   NOT NULL,
    end_time         TIME   NOT NULL,
    status           ENUM('AVAILABLE', 'BOOKED', 'CANCELLED')
                            NOT NULL DEFAULT 'AVAILABLE',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (slot_id),
    KEY idx_slots_professional_date (professional_id, slot_date, start_time),
    CONSTRAINT fk_slot_professional
        FOREIGN KEY (professional_id)
        REFERENCES professional_profiles (professional_id),
    CONSTRAINT chk_slot_times CHECK (end_time > start_time)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- appointments: at most ONE row per slot (UNIQUE slot_id). A cancelled
-- or rejected row is re-used when the slot is booked again (see
-- AppointmentDao.createAppointment).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id   BIGINT NOT NULL AUTO_INCREMENT,
    slot_id          BIGINT NOT NULL,
    patient_id       BIGINT NOT NULL,
    professional_id  BIGINT NOT NULL,
    status           ENUM('PENDING', 'CONFIRMED', 'REJECTED',
                          'CANCELLED', 'COMPLETED')
                            NOT NULL DEFAULT 'PENDING',
    reason           VARCHAR(2000) NULL,
    booking_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at     TIMESTAMP NULL DEFAULT NULL,
    completed_at     TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (appointment_id),
    UNIQUE KEY uq_appointments_slot (slot_id),
    KEY idx_appointments_patient (patient_id, status),
    KEY idx_appointments_professional (professional_id, status),
    CONSTRAINT fk_appointment_slot
        FOREIGN KEY (slot_id) REFERENCES availability_slots (slot_id),
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id) REFERENCES patient_profiles (patient_id),
    CONSTRAINT fk_appointment_professional
        FOREIGN KEY (professional_id)
        REFERENCES professional_profiles (professional_id)
) ENGINE = InnoDB;
