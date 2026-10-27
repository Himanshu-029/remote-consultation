
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Availability | Remote Consultation</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            padding: 32px 16px;
            font-family: Arial, sans-serif;
            color: #172033;
            background: linear-gradient(135deg, #eef4ff, #f8fafc);
        }

        .container {
            width: 100%;
            max-width: 620px;
            margin: 35px auto;
        }

        .card {
            padding: 32px;
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 18px;
            box-shadow: 0 15px 45px rgba(15, 23, 42, 0.08);
        }

        .eyebrow {
            color: #2563eb;
            font-size: 12px;
            font-weight: bold;
            letter-spacing: 1.5px;
            text-transform: uppercase;
        }

        h1 {
            margin: 10px 0;
            font-size: 28px;
        }

        .subtitle {
            margin-bottom: 26px;
            color: #64748b;
            line-height: 1.6;
        }

        label {
            display: block;
            margin: 18px 0 8px;
            font-size: 14px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 13px;
            border: 1px solid #cbd5e1;
            border-radius: 9px;
            font-size: 15px;
        }

        input:focus {
            outline: 2px solid #bfdbfe;
            border-color: #2563eb;
        }

        .time-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }

        button {
            width: 100%;
            margin-top: 25px;
            padding: 14px;
            border: 0;
            border-radius: 9px;
            background: #2563eb;
            color: white;
            font-size: 15px;
            font-weight: bold;
            cursor: pointer;
        }

        button:hover {
            background: #1d4ed8;
        }

        .message {
            margin-bottom: 18px;
            padding: 13px;
            border-radius: 9px;
            line-height: 1.5;
        }

        .success {
            color: #166534;
            background: #dcfce7;
        }

        .error {
            color: #991b1b;
            background: #fee2e2;
        }

        .back {
            display: inline-block;
            margin-top: 20px;
            color: #2563eb;
            text-decoration: none;
        }

        @media (max-width: 480px) {
            .card {
                padding: 22px;
            }

            .time-grid {
                grid-template-columns: 1fr;
                gap: 0;
            }
        }
    </style>
</head>

<body>
<main class="container">
    <section class="card">
        <div class="eyebrow">Professional workspace</div>
        <h1>Manage availability</h1>
        <p class="subtitle">
            Publish a future consultation time so patients can book
            an appointment when booking becomes available.
        </p>

        <c:if test="${not empty successMessage}">
            <div class="message success" role="status">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="message error" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/professional/availability">

            <label for="slotDate">Availability date</label>
            <input type="date"
                   id="slotDate"
                   name="slotDate"
                   required>

            <div class="time-grid">
                <div>
                    <label for="startTime">Start time</label>
                    <input type="time"
                           id="startTime"
                           name="startTime"
                           required>
                </div>

                <div>
                    <label for="endTime">End time</label>
                    <input type="time"
                           id="endTime"
                           name="endTime"
                           required>
                </div>
            </div>

            <button type="submit">Publish availability</button>
        </form>

        <a class="back"
           href="${pageContext.request.contextPath}/dashboard">
            &larr; Back to dashboard
        </a>
    </section>
</main>

<script>
    const dateInput = document.getElementById("slotDate");

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const localDate = [
        tomorrow.getFullYear(),
        String(tomorrow.getMonth() + 1).padStart(2, "0"),
        String(tomorrow.getDate()).padStart(2, "0")
    ].join("-");

    dateInput.min = localDate;

    document.querySelector("form").addEventListener("submit", function (event) {
        const start = document.getElementById("startTime").value;
        const end = document.getElementById("endTime").value;

        if (start && end && end <= start) {
            event.preventDefault();
            alert("End time must be later than start time.");
        }
    });
</script>
</body>
</html>
