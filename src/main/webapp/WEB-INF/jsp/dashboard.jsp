
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | Remote Consultation</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7fb;
            color: #1f2937;
        }

        .navbar {
            background: #2563eb;
            color: white;
            padding: 18px 35px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
        }

        .navbar h2 {
            margin: 0;
        }

        .user-info {
            text-align: right;
            overflow-wrap: anywhere;
        }

        .user-name {
            font-weight: bold;
        }

        .user-role {
            font-size: 13px;
            opacity: 0.9;
        }

        .container {
            width: 90%;
            max-width: 1100px;
            margin: 35px auto;
        }

        .welcome-card {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.07);
            margin-bottom: 25px;
        }

        .welcome-card h1 {
            margin-top: 0;
            color: #2563eb;
        }

        .verification-notice {
            margin-top: 20px;
            padding: 15px 18px;
            background: #fff7ed;
            border: 1px solid #fed7aa;
            border-left: 5px solid #f59e0b;
            border-radius: 8px;
            color: #9a3412;
            line-height: 1.5;
        }

        .verification-notice strong {
            display: block;
            margin-bottom: 5px;
        }

        .verification-notice.approved {
            background: #ecfdf5;
            border-color: #a7f3d0;
            border-left-color: #10b981;
            color: #065f46;
        }

        .verification-notice.rejected {
            background: #fff0ef;
            border-color: #fecaca;
            border-left-color: #dc2626;
            color: #991b1b;
        }

        .cards {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 20px;
        }

        .card {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.07);
            min-width: 0;
        }

        .card h3 {
            margin-top: 0;
            color: #2563eb;
        }

        .card p {
            color: #6b7280;
            line-height: 1.5;
        }

        .action-btn {
            display: inline-block;
            margin-top: 12px;
            padding: 11px 20px;
            background: #2563eb;
            color: white;
            text-decoration: none;
            border: none;
            border-radius: 6px;
            font-family: inherit;
            font-size: inherit;
            font-weight: bold;
            cursor: pointer;
        }

        .action-btn:hover {
            background: #1d4ed8;
        }

        .logout-btn {
            margin-top: 25px;
            padding: 11px 20px;
            background: #dc2626;
            color: white;
            border: none;
            border-radius: 6px;
            font-family: inherit;
            font-size: inherit;
            font-weight: bold;
            cursor: pointer;
        }

        .logout-btn:hover {
            background: #b91c1c;
        }

        .action-btn:focus-visible,
        .logout-btn:focus-visible {
            outline: 3px solid #93c5fd;
            outline-offset: 3px;
        }

        @media (max-width: 800px) {
            .cards {
                grid-template-columns: 1fr;
            }

            .navbar {
                padding: 15px 20px;
            }

            .container {
                width: 95%;
            }
        }
    </style>
</head>

<body>

<nav class="navbar">
    <h2>Remote Consultation</h2>

    <div class="user-info">
        <div class="user-name">
            <c:out value="${sessionScope.userName}" default="User" />
        </div>

        <div class="user-role">
            <c:out value="${sessionScope.role}" />
        </div>
    </div>
</nav>

<main class="container">

    <section class="welcome-card">
        <h1>
            Welcome,
            <c:out value="${sessionScope.userName}" default="User" />!
        </h1>

        <p>
            You are successfully logged in to the Remote Consultation
            platform.
        </p>

        <p>
            Your account role is:
            <strong><c:out value="${sessionScope.role}" /></strong>
        </p>

        <c:if test="${sessionScope.role eq 'PROFESSIONAL'}">

            <c:choose>
                <c:when test="${verificationStatus eq 'PENDING'}">
                    <div class="verification-notice" role="status">
                        <strong>Verification Pending</strong>
                        Your professional profile is awaiting administrator
                        review. Services requiring approval remain unavailable
                        until your profile is approved.
                    </div>
                </c:when>

                <c:when test="${verificationStatus eq 'REJECTED'}">
                    <div class="verification-notice rejected" role="status">
                        <strong>Verification Rejected</strong>
                        Your professional profile was not approved. Please
                        contact the administrator for clarification.
                    </div>
                </c:when>

                <c:when test="${verificationStatus eq 'APPROVED'}">
                    <div class="verification-notice approved" role="status">
                        <strong>Verification Approved</strong>
                        Your professional profile has been approved.
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="verification-notice" role="status">
                        <strong>Verification Status Unavailable</strong>
                        We could not confirm your verification status.
                        Please try again later or contact the administrator.
                    </div>
                </c:otherwise>
            </c:choose>

        </c:if>
    </section>

    <section class="cards" aria-label="Dashboard actions">

        <c:choose>

            <c:when test="${sessionScope.role eq 'ADMIN'}">
                <article class="card">
                    <h3>Professional Verification</h3>
                    <p>
                        Review pending professional applications, verify
                        credentials, and approve or reject registrations.
                    </p>
                    <a href="${pageContext.request.contextPath}/admin/verification"
                       class="action-btn">
                        Open Verification Panel
                    </a>
                </article>
            </c:when>

            <c:when test="${sessionScope.role eq 'PROFESSIONAL'}">

                <article class="card">
                    <h3>Professional Profile</h3>
                    <p>
                        Check your verification status and manage your
                        professional account.
                    </p>
                </article>

                <article class="card">
                    <h3>Appointments</h3>
                    <p>
                        View and manage your patient appointments.
                    </p>
                    <a href="${pageContext.request.contextPath}/professional/appointments"
                       class="action-btn">
                        Manage Appointments
                    </a>
                </article>

                <article class="card">
                    <h3>Manage Availability</h3>
                    <p>
                        Publish available consultation dates and time slots.
                        Server-side verification is required for restricted
                        actions.
                    </p>
                    <c:choose>
                        <c:when test="${verificationStatus eq 'APPROVED'}">
                            <a href="${pageContext.request.contextPath}/professional/availability"
                               class="action-btn">
                                Manage Availability
                            </a>
                        </c:when>
                        <c:otherwise>
                            <p>
                                Availability management is unavailable until
                                your professional profile is approved.
                            </p>
                        </c:otherwise>
                    </c:choose>
                </article>

            </c:when>

            <c:when test="${sessionScope.role eq 'PATIENT'}">

                <article class="card">
                    <h3>Find Healthcare Professionals</h3>
                    <p>
                        Search for healthcare professionals by specialization
                        and availability.
                    </p>
                    <a href="${pageContext.request.contextPath}/patient/professionals"
                       class="action-btn">
                        Find Professionals
                    </a>
                </article>

                <article class="card">
                    <h3>Appointments</h3>
                    <p>
                        View your appointment history and manage eligible
                        bookings.
                    </p>
                    <a href="${pageContext.request.contextPath}/patient/appointments"
                       class="action-btn">
                        My Appointments
                    </a>
                </article>

                <article class="card">
                    <h3>Consultations</h3>
                    <p>
                        Remote consultation features will be available as
                        those workflows are implemented.
                    </p>
                </article>

            </c:when>

            <c:otherwise>
                <article class="card">
                    <h3>Dashboard unavailable</h3>
                    <p>
                        Your account role could not be recognized.
                        Please sign in again.
                    </p>
                    <a href="${pageContext.request.contextPath}/login"
                       class="action-btn">
                        Return to Login
                    </a>
                </article>
            </c:otherwise>

        </c:choose>

    </section>

    <form action="${pageContext.request.contextPath}/logout"
          method="post">

        <input type="hidden"
               name="_csrf"
               value="<c:out value='${sessionScope.csrfToken}' />">

        <button type="submit" class="logout-btn">
            Logout
        </button>
    </form>

</main>
</body>
</html>
