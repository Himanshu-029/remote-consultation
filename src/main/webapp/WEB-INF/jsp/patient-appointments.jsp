
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Appointments | Remote Consultation</title>
    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7fb;
            color: #1f2937;
        }

        header {
            background: #123b63;
            color: white;
            padding: 22px 6%;
        }

        header h1 { margin: 0; font-size: 25px; }

        main {
            max-width: 1100px;
            margin: 35px auto;
            padding: 0 20px;
        }

        .top-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 14px;
            margin-bottom: 24px;
        }

        .top-row h2 { margin: 0 0 8px; }
        .muted { color: #64748b; }

        .back-link, .action-link {
            display: inline-block;
            padding: 11px 16px;
            background: #123b63;
            color: white;
            text-decoration: none;
            border-radius: 8px;
        }

        .appointment-card {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 22px;
            margin-bottom: 18px;
            box-shadow: 0 4px 14px rgba(15, 23, 42, .04);
        }

        .card-heading {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }

        .card-heading h3 { margin: 0; }

        .status {
            display: inline-block;
            border-radius: 20px;
            padding: 7px 12px;
            font-size: 12px;
            font-weight: bold;
            background: #fef3c7;
            color: #92400e;
        }

        .status.CONFIRMED,
        .status.COMPLETED {
            background: #dcfce7;
            color: #166534;
        }

        .status.CANCELLED,
        .status.REJECTED {
            background: #fee2e2;
            color: #991b1b;
        }

        .details {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
            gap: 18px;
            margin-top: 20px;
        }

        .detail-label {
            display: block;
            font-size: 12px;
            color: #64748b;
            margin-bottom: 6px;
        }

        .detail-value {
            font-weight: 600;
            overflow-wrap: anywhere;
        }

        .reason {
            margin-top: 18px;
            padding-top: 15px;
            border-top: 1px solid #e2e8f0;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }

        .empty {
            text-align: center;
            background: white;
            border: 1px dashed #cbd5e1;
            border-radius: 14px;
            padding: 45px 20px;
        }

        .notice {
            padding: 14px 16px;
            margin-bottom: 20px;
            border-radius: 9px;
            font-weight: 600;
        }

        .notice.success {
            color: #166534;
            background: #dcfce7;
            border: 1px solid #86efac;
        }

        .notice.error {
            color: #991b1b;
            background: #fee2e2;
            border: 1px solid #fca5a5;
        }

        .appointment-actions {
            margin-top: 20px;
            padding-top: 16px;
            border-top: 1px solid #e2e8f0;
        }

        .cancel-button {
            padding: 11px 16px;
            border: none;
            border-radius: 8px;
            background: #b91c1c;
            color: white;
            font-weight: 600;
            cursor: pointer;
        }

        .cancel-button:hover { background: #991b1b; }

        @media (max-width: 600px) {
            main { margin-top: 22px; }
            .appointment-card { padding: 17px; }
        }
    </style>
</head>
<body>

<header>
    <h1>Remote Consultation</h1>
</header>

<main>
    <div class="top-row">
        <div>
            <h2>My Appointments</h2>
            <p class="muted">Review your bookings and appointment statuses.</p>
        </div>
        <a class="back-link"
           href="${pageContext.request.contextPath}/dashboard">
            Back to Dashboard
        </a>
    </div>

    <c:if test="${param.cancel eq 'success'}">
        <div class="notice success" role="status">
            Appointment cancelled successfully.
        </div>
    </c:if>

    <c:if test="${param.cancel eq 'error'}">
        <div class="notice error" role="alert">
            We couldn't cancel this appointment. It may have already changed
            status, or it may not belong to your account. Please refresh and try again.
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty appointments}">
            <div class="empty">
                <h3>No appointments yet</h3>
                <p class="muted">
                    Your bookings will appear here after you schedule a consultation.
                </p>
                <a class="back-link"
                   href="${pageContext.request.contextPath}/patient/professionals">
                    Find a Professional
                </a>
            </div>
        </c:when>

        <c:otherwise>
            <c:forEach var="appointment" items="${appointments}">
                <section class="appointment-card">
                    <div class="card-heading">
                        <h3>
                            Appointment #<c:out value="${appointment.appointmentId}"/>
                        </h3>
                        <span class="status ${appointment.status}">
                            <c:out value="${appointment.status}"/>
                        </span>
                    </div>

                    <div class="details">
                        <div>
                            <span class="detail-label">Professional</span>
                            <span class="detail-value">
                                <c:out value="${appointment.professionalName}"/>
                            </span>
                        </div>

                        <div>
                            <span class="detail-label">Specialization</span>
                            <span class="detail-value">
                                <c:out value="${appointment.specializationName}"
                                       default="Not specified"/>
                            </span>
                        </div>

                        <div>
                            <span class="detail-label">Date</span>
                            <span class="detail-value">
                                <c:out value="${appointment.slotDate}"/>
                            </span>
                        </div>

                        <div>
                            <span class="detail-label">Time</span>
                            <span class="detail-value">
                                <c:out value="${appointment.startTime}"/>
                                – <c:out value="${appointment.endTime}"/>
                            </span>
                        </div>

                        <div>
                            <span class="detail-label">Booked on</span>
                            <span class="detail-value">
                                <c:out value="${appointment.bookingTime}"
                                       default="Not available"/>
                            </span>
                        </div>
                    </div>

                    <c:if test="${not empty appointment.reason}">
                        <div class="reason">
                            <span class="detail-label">Consultation reason</span>
                            <c:out value="${appointment.reason}"/>
                        </div>
                    </c:if>

                    <c:if test="${appointment.status eq 'PENDING'
                                  or appointment.status eq 'CONFIRMED'}">
                        <div class="appointment-actions">
                            <form method="post"
                                  action="${pageContext.request.contextPath}/patient/cancel-appointment"
                                  onsubmit="return confirm('Are you sure you want to cancel this appointment?');">

                                <input type="hidden"
                                        name="_csrf"
                                        value="<c:out value='${sessionScope.csrfToken}'/>">
                                <input type="hidden"
                                        name="appointmentId"
                                        value="<c:out value='${appointment.appointmentId}'/>">
                                <button type="submit" class="cancel-button">
                                    Cancel Appointment
                                </button>
                            </form>
                        </div>
                    </c:if>
                </section>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</main>

</body>
</html>
