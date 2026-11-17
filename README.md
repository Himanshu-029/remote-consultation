# Remote Consultation

A web application that lets patients find verified healthcare professionals, book appointment slots, and manage their consultations online. Built with **Java 21, Spring Boot, Servlets, JSP, JDBC and MySQL**.

> **Status:** in active development. The appointment workflow is complete; consultation sessions, notifications, reviews, medical records and payments are planned (see [Roadmap](#roadmap)).

## Features

**Patients**
- Register and log in, browse approved professionals, and view their available slots
- Book, view and cancel appointments

**Professionals**
- Register with qualification and licence details (account starts as *pending*)
- After admin approval: publish availability slots (overlap-checked), confirm or reject requests, and mark confirmed appointments as completed once they have ended

**Admins**
- Review pending professional registrations and approve or reject them

**Security**
- BCrypt password hashing, session-fixation protection on login, CSRF tokens on every state-changing request
- Role-based access control (`PATIENT`, `PROFESSIONAL`, `ADMIN`) enforced in filters and again in each servlet
- Login rate limiting (5 failed attempts / 15 minutes), no stack traces or server header in responses
- Double-booking is prevented with row locks in a single transaction and a unique constraint on `appointments.slot_id`

## Tech stack

| Layer      | Technology                                    |
|------------|-----------------------------------------------|
| Language   | Java 21                                       |
| Framework  | Spring Boot (embedded Tomcat), Servlets, JSP + JSTL |
| Data       | Plain JDBC (DAO layer), HikariCP, MySQL 8     |
| Build      | Maven (WAR packaging)                         |
| Testing    | JUnit 5, Mockito, MySQL integration tests     |

## Getting started

### Prerequisites
JDK 21, Maven, and MySQL 8 running locally.

### 1. Create the database
```bash
mysql -u root -p < database/schema.sql
```
This creates the `remote_consultation` database, all tables, and the six specializations used by the registration form. It is safe to run more than once.

### 2. Set environment variables

| Variable             | Required | Purpose                                                        |
|----------------------|----------|----------------------------------------------------------------|
| `DB_PASSWORD`        | yes      | Password of the MySQL `root` user (see `application.properties`) |
| `APP_ADMIN_EMAIL`    | first run | Email of the first admin account                              |
| `APP_ADMIN_PASSWORD` | first run | Its password (8+ characters)                                  |
| `APP_ADMIN_NAME`     | no       | Display name (default `Administrator`)                         |

The admin is created at startup only if no user with that email exists. No default credentials are stored in the repository.

```powershell
# Windows PowerShell
$env:DB_PASSWORD = "your-mysql-password"
$env:APP_ADMIN_EMAIL = "admin@example.com"
$env:APP_ADMIN_PASSWORD = "choose-a-strong-password"
```
```bash
# macOS / Linux
export DB_PASSWORD="your-mysql-password"
export APP_ADMIN_EMAIL="admin@example.com"
export APP_ADMIN_PASSWORD="choose-a-strong-password"
```

### 3. Run
```bash
mvn spring-boot:run
```
Open <http://localhost:8080/login>.

### Try the full flow
1. Register a professional at `/professional-register`, then log in as the admin and approve them at `/admin/verification`.
2. Log in as the professional and add availability at `/professional/availability` (slots must be on a future date).
3. Register a patient at `/register`, open **Find a professional**, and book a slot.
4. As the professional, confirm the request at `/professional/appointments`; once the slot has ended, mark it completed.

## Routes

| Route | Method | Role | Description |
|-------|--------|------|-------------|
| `/login`, `/register`, `/professional-register` | GET/POST | public | Sign in and registration |
| `/logout` | POST | any | Sign out |
| `/dashboard` | GET | any | Role-specific home page |
| `/patient/professionals` | GET | patient | Directory of approved professionals |
| `/patient/professional-availability?professionalId=` | GET | patient | Available slots |
| `/patient/book-appointment` | POST | patient | Book a slot |
| `/patient/appointments` | GET | patient | Appointment history |
| `/patient/cancel-appointment` | POST | patient | Cancel a pending or confirmed appointment |
| `/professional/availability` | GET/POST | professional | Publish slots |
| `/professional/appointments` | GET/POST | professional | Confirm, reject or complete |
| `/admin/verification` | GET/POST | admin | Approve or reject professionals |

## Time zone

All "is this slot still in the future?" rules use one application clock, configured by `app.timezone` in `application.properties` (default `Asia/Kolkata`). It is also used for the JDBC `serverTimezone`, so the app no longer depends on the JVM or MySQL server time zone for these decisions. The audit columns (`booking_time`, `cancelled_at`, `completed_at`) still use MySQL's `CURRENT_TIMESTAMP`.

## Tests

```bash
mvn test
```
Unit tests use mocks and need nothing else. The `*MySqlTest` classes are integration tests: they connect to the local `remote_consultation` database as `root` (using `DB_PASSWORD`), create temporary rows and delete them afterwards, so run `database/schema.sql` first.

## Project structure

```
database/schema.sql              MySQL schema + seed data
src/main/java/com/remoteconsultation/
  config/      Servlet/filter registration, transaction helper, clock, admin bootstrap
  filter/      Authentication, CSRF and role-authorization filters
  servlet/     HTTP endpoints
  service/     Business rules and transactions
  dao/         JDBC queries
  model/ dto/  Domain objects and request objects
  security/ util/  Login rate limiter, password hashing, validation
src/main/webapp/WEB-INF/jsp/     JSP views
src/test/                        Unit and integration tests
```

## Roadmap

- [x] Registration, login, roles, professional verification
- [x] Availability slots and appointment booking / confirmation / cancellation / completion
- [ ] Consultation session (video-meeting link for confirmed appointments)
- [ ] In-app notifications
- [ ] Patient reviews after completed appointments
- [ ] Medical records and prescriptions
- [ ] Online payments
