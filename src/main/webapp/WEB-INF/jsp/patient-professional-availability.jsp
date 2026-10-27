
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Available Appointment Slots | Remote Consultation</title>
    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            padding: 32px 18px;
            font-family: Arial, sans-serif;
            background: #f3f6fb;
            color: #172033;
        }

        .container {
            max-width: 900px;
            margin: auto;
        }

        .back {
            display: inline-block;
            margin-bottom: 22px;
            color: #3157c8;
            text-decoration: none;
            font-weight: 600;
        }

        .panel {
            padding: 30px;
            background: #fff;
            border-radius: 18px;
            box-shadow: 0 8px 28px rgba(30, 50, 90, .08);
        }

        h1 { margin-top: 0; font-size: 28px; }
        .subtitle { color: #647087; line-height: 1.6; }

        .slot-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
            margin-top: 24px;
        }

        .slot {
            padding: 20px;
            border: 1px solid #dce5f5;
            border-radius: 12px;
        }

        .date {
            font-weight: 700;
            margin-bottom: 10px;
        }

        .time {
            color: #3157c8;
            font-size: 17px;
            font-weight: 700;
            margin-bottom: 18px;
        }

        .book-button {
            display: inline-block;
            padding: 11px 16px;
            border-radius: 8px;
            background: #3157c8;
            color: white;
            text-decoration: none;
            font-weight: 600;
        }

        .empty {
            margin-top: 24px;
            padding: 22px;
            border-radius: 10px;
            background: #f6f8fc;
            color: #647087;
        }

        @media (max-width: 600px) {
            .panel { padding: 22px 16px; }
            h1 { font-size: 23px; }
        }
    </style>
</head>
<body>
<div class="container">
    <a class="back" href="${pageContext.request.contextPath}/patient/professionals">
        &larr; Back to professionals
    </a>

    <main class="panel">
        <h1>Available appointment slots</h1>
        <p class="subtitle">
            Choose a suitable date and time for your consultation.
        </p>


<c:if test="${param.booking == 'success'}">
    <div style="padding:14px 16px; margin:18px 0; border-radius:10px; background:#e8f8ee; color:#176b3a;">
        <strong>Appointment booked successfully!</strong>
        <c:if test="${not empty param.appointmentId}">
            <div>Appointment ID:
                <c:out value="${param.appointmentId}"/>
            </div>
        </c:if>
        Your appointment is pending confirmation.
    </div>
</c:if>


<c:if test="${param.booking == 'error'}">
    <div style="padding:14px 16px; margin:18px 0; border-radius:10px; background:#fff0f0; color:#a12626;">
        <strong>Booking unsuccessful</strong>
        <div>
            <c:out value="${param.message}"
                   default="This slot may no longer be available. Please choose another time."/>
        </div>
    </div>
</c:if>



        <c:choose>
            <c:when test="${not empty slots}">
                <div class="slot-list">
                    <c:forEach var="slot" items="${slots}">
                        <article class="slot">
                            <div class="date">
                                <c:out value="${slot.slotDate}"/>
                            </div>

                            <div class="time">
                                <c:out value="${slot.startTime}"/>
                                &ndash;
                                <c:out value="${slot.endTime}"/>
                            </div>


<form method="post"
      action="${pageContext.request.contextPath}/patient/book-appointment">

    <input type="hidden" name="slotId"
           value="<c:out value='${slot.slotId}'/>">

    <input type="hidden" name="professionalId"
           value="<c:out value='${professionalId}'/>">

    <label for="reason-${slot.slotId}">Reason for consultation (optional)</label>
    <textarea id="reason-${slot.slotId}"
              name="reason"
              maxlength="2000"
              rows="3"
              style="width:100%; margin:8px 0 14px; padding:10px; border:1px solid #dce5f5; border-radius:8px;"
              placeholder="Briefly describe your concern"></textarea>

    <button class="book-button" type="submit"
            style="border:0; cursor:pointer;">
        Book appointment
    </button>
</form>

                        </article>
                    </c:forEach>
                </div>
            </c:when>

            <c:otherwise>
                <div class="empty">
                    No future availability is listed for this professional yet.
                    Please check again later or choose another professional.
                </div>
            </c:otherwise>
        </c:choose>
    </main>
</div>
</body>
</html>
