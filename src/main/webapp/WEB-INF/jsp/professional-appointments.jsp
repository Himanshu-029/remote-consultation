
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Appointments | Remote Consultation</title>

    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f3f6fb;
            color: #172033;
        }

        .header {
            background: #123b66;
            color: white;
            padding: 22px 6%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
        }

        .header h1 { margin: 0; font-size: 24px; }
        .header p { margin: 7px 0 0; color: #dce9f7; }

        .nav-link {
            color: white;
            text-decoration: none;
            border: 1px solid #a9c4e3;
            padding: 10px 15px;
            border-radius: 8px;
        }

        .container {
            max-width: 1100px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .notice {
            padding: 14px 17px;
            border-radius: 9px;
            margin-bottom: 20px;
        }

        .success { background: #dcfce7; color: #166534; }
        .error { background: #fee2e2; color: #991b1b; }

        .intro { margin-bottom: 22px; }
        .intro h2 { margin-bottom: 8px; }
        .intro p { color: #64748b; }

        .appointment-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(290px, 1fr));
            gap: 20px;
        }

        .card {
            background: white;
            border: 1px solid #e1e8f0;
            border-radius: 14px;
            padding: 22px;
            box-shadow: 0 5px 18px rgba(15, 40, 70, 0.05);
        }

        .card-top {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 10px;
        }

        .card h3 { margin: 0 0 6px; font-size: 18px; }
        .muted { color: #64748b; font-size: 13px; }

        .status {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
            background: #e2e8f0;
            color: #334155;
            white-space: nowrap;
        }

        .status-pending {
            background: #fef3c7;
            color: #92400e;
        }

        .status-confirmed {
            background: #dcfce7;
            color: #166534;
        }

        .status-rejected {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-completed {
            background: #dbeafe;
            color: #1d4ed8;
        }

        .status-cancelled {
            background: #f1f5f9;
            color: #475569;
        }

        .details {
            border-top: 1px solid #edf0f5;
            margin-top: 18px;
            padding-top: 15px;
        }

        .detail { margin: 0 0 12px; }

        .detail strong {
            display: block;
            font-size: 12px;
            color: #64748b;
            margin-bottom: 4px;
        }

        .actions {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }

        .actions form { flex: 1; }

        .btn {
            display: inline-block;
            width: 100%;
            padding: 11px 12px;
            border: 0;
            border-radius: 8px;
            font-weight: bold;
            cursor: pointer;
            font-size: 14px;
        }

        .confirm { background: #15803d; color: white; }
        .reject { background: #b91c1c; color: white; }
        .complete { background: #1d4ed8; color: white; }

        .btn:hover { filter: brightness(.94); }

        .empty {
            background: white;
            padding: 40px 20px;
            text-align: center;
            border-radius: 14px;
            color: #64748b;
        }

        @media (max-width: 500px) {
            .header { padding: 20px; }
            .container { padding: 0 14px; }
            .actions { flex-direction: column; }
        }
    </style>
</head>

<body>
<header class="header">
    <div>
        <h1>Appointment Management</h1>
        <p>Review bookings and manage your patient appointments.</p>
    </div>

    <a class="nav-link"
       href="${pageContext.request.contextPath}/dashboard">
        Back to Dashboard
    </a>
</header>

<main class="container">

    <c:if test="${param.updated == 'confirmed'}">
        <div class="notice success" role="status">
            Appointment confirmed successfully.
        </div>
    </c:if>

    <c:if test="${param.updated == 'rejected'}">
        <div class="notice success" role="status">
            Appointment rejected successfully.
        </div>
    </c:if>

    <c:if test="${param.updated == 'completed'}">
        <div class="notice success" role="status">
            Appointment marked as completed successfully.
        </div>
    </c:if>

    <c:if test="${not empty param.error}">
    <div class="notice error" role="alert">
        <c:out value="${param.error}"/>
    </div>
</c:if>

    <section class="intro">
        <h2>Your appointments</h2>
        <p>
            Review bookings, confirm or reject pending requests, and mark
            confirmed appointments as completed after the consultation.
        </p>
    </section>

    <c:choose>
        <c:when test="${empty appointments}">
            <div class="empty">
                <h3>No appointments yet</h3>
                <p>New patient bookings will appear here.</p>
            </div>
        </c:when>

        <c:otherwise>
            <div class="appointment-list">
                <c:forEach var="appointment" items="${appointments}">
                    <article class="card">

                        <div class="card-top">
                            <div>
                                <h3>
                                    <c:out value="${appointment.patientName}"
                                           default="Patient"/>
                                </h3>

                                <span class="muted">
                                    Appointment #
                                    <c:out value="${appointment.appointmentId}"/>
                                </span>
                            </div>

                            <c:set var="statusClass"
                                   value="${appointment.status == 'PENDING' ? 'status-pending' :
                                           appointment.status == 'CONFIRMED' ? 'status-confirmed' :
                                           appointment.status == 'REJECTED' ? 'status-rejected' :
                                           appointment.status == 'COMPLETED' ? 'status-completed' :
                                           appointment.status == 'CANCELLED' ? 'status-cancelled' : ''}"/>

                            <span class="status ${statusClass}">
                                <c:out value="${appointment.status}"/>
                            </span>
                        </div>

                        <div class="details">
                            <p class="detail">
                                <strong>Date</strong>
                                <c:out value="${appointment.slotDate}"/>
                            </p>

                            <p class="detail">
                                <strong>Time</strong>
                                <c:out value="${appointment.startTime}"/>
                                –
                                <c:out value="${appointment.endTime}"/>
                            </p>

                            <p class="detail">
                                <strong>Reason for consultation</strong>
                                <c:choose>
                                    <c:when test="${not empty appointment.reason}">
                                        <c:out value="${appointment.reason}"/>
                                    </c:when>
                                    <c:otherwise>Not provided</c:otherwise>
                                </c:choose>
                            </p>

                            <p class="detail">
                                <strong>Booking submitted</strong>
                                <c:out value="${appointment.bookingTime}"/>
                            </p>
                        </div>

                        <%-- Pending appointments: confirm or reject --%>
                        <c:if test="${appointment.status == 'PENDING'}">
                            <div class="actions">

                                <form method="post"
                                      action="${pageContext.request.contextPath}/professional/appointments"
                                      onsubmit="return confirm('Confirm this appointment?');">

                                    <input type="hidden"
                                           name="appointmentId"
                                           value="<c:out value='${appointment.appointmentId}'/>">

                                    <input type="hidden"
                                           name="action"
                                           value="confirm">

                                    <button class="btn confirm" type="submit">
                                        Confirm
                                    </button>
                                </form>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/professional/appointments"
                                      onsubmit="return confirm('Reject this appointment?');">

                                    <input type="hidden"
                                           name="appointmentId"
                                           value="<c:out value='${appointment.appointmentId}'/>">

                                    <input type="hidden"
                                           name="action"
                                           value="reject">

                                    <button class="btn reject" type="submit">
                                        Reject
                                    </button>
                                </form>

                            </div>
                        </c:if>

                        <%-- Confirmed appointments: mark completed --%>
                        <c:if test="${appointment.status == 'CONFIRMED'}">
                            <div class="actions">
                                <form method="post"
                                      action="${pageContext.request.contextPath}/professional/appointments"
                                      onsubmit="return confirm('Mark this appointment as completed?');">

                                    <input type="hidden"
                                           name="appointmentId"
                                           value="<c:out value='${appointment.appointmentId}'/>">

                                    <input type="hidden"
                                           name="action"
                                           value="complete">

                                    <button class="btn complete" type="submit">
                                        Mark as Completed
                                    </button>
                                </form>
                            </div>
                        </c:if>

                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

</main>
</body>
</html>
