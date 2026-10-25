<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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
        }

        .navbar h2 {
            margin: 0;
        }

        .user-info {
            text-align: right;
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

        .cards {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
        }

        .card {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.07);
        }

        .card h3 {
            margin-top: 0;
            color: #2563eb;
        }

        .card p {
            color: #6b7280;
            line-height: 1.5;
        }

        .logout-btn {
            display: inline-block;
            margin-top: 25px;
            padding: 11px 20px;
            background: #dc2626;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            font-weight: bold;
        }

        .logout-btn:hover {
            background: #b91c1c;
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
            ${sessionScope.userName}
        </div>

        <div class="user-role">
            ${sessionScope.role}
        </div>

    </div>

</nav>

<div class="container">

    <div class="welcome-card">

        <h1>
            Welcome, ${sessionScope.userName}!
        </h1>

        <p>
            You are successfully logged in to the Remote Consultation
            platform.
        </p>

        <p>
            Your account role is:
            <strong>${sessionScope.role}</strong>
        </p>

        <% if ("PROFESSIONAL".equals(session.getAttribute("role"))
                && "PENDING".equals(request.getAttribute("verificationStatus"))) { %>

            <div class="verification-notice">
                <strong>Verification Pending</strong>
                Your professional account is awaiting admin verification.
                You can access your dashboard, but consultation booking
                features will be available after your profile is approved.
            </div>

        <% } %>

    </div>

    <div class="cards">

        <div class="card">
            <h3>Find Healthcare Professionals</h3>
            <p>
                Search and explore healthcare professionals
                based on specialization and availability.
            </p>
        </div>

        <div class="card">
            <h3>Appointments</h3>
            <p>
                View, book, cancel, and manage your
                consultation appointments.
            </p>
        </div>

        <div class="card">
            <h3>Consultations</h3>
            <p>
                Access your remote consultations, medical
                records, and prescriptions.
            </p>
        </div>

    </div>

    <a
        href="${pageContext.request.contextPath}/logout"
        class="logout-btn">
        Logout
    </a>

</div>

</body>
</html>