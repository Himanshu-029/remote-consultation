
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Professional Registration</title>

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

        .container {
            width: 90%;
            max-width: 900px;
            margin: 40px auto;
        }

        .card {
            background: white;
            padding: 35px;
            border-radius: 14px;
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
        }

        h1 {
            margin-bottom: 8px;
            color: #1e3a8a;
        }

        .subtitle {
            color: #6b7280;
            margin-bottom: 30px;
            line-height: 1.5;
        }

        .section-title {
            margin-top: 25px;
            margin-bottom: 15px;
            color: #1e40af;
            border-bottom: 1px solid #e5e7eb;
            padding-bottom: 8px;
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 18px;
        }

        .field {
            display: flex;
            flex-direction: column;
        }

        .full {
            grid-column: 1 / -1;
        }

        label {
            margin-bottom: 7px;
            font-weight: 600;
        }

        input,
        select,
        textarea {
            padding: 11px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 15px;
            font-family: inherit;
        }

        textarea {
            resize: vertical;
            min-height: 100px;
        }

        input:focus,
        select:focus,
        textarea:focus {
            outline: none;
            border-color: #2563eb;
        }

        .button {
            margin-top: 25px;
            width: 100%;
            padding: 13px;
            border: none;
            border-radius: 8px;
            background: #2563eb;
            color: white;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
        }

        .button:hover {
            background: #1d4ed8;
        }

        .message {
            padding: 12px 15px;
            margin: 20px 0;
            border-radius: 8px;
            font-weight: 600;
            line-height: 1.5;
        }

        .success-message {
            color: #166534;
            background: #dcfce7;
            border: 1px solid #86efac;
        }

        .error-message {
            color: #991b1b;
            background: #fee2e2;
            border: 1px solid #fca5a5;
        }

        .login-link {
            text-align: center;
            margin-top: 20px;
        }

        .login-link a {
            color: #2563eb;
            text-decoration: none;
        }

        .login-link a:hover {
            text-decoration: underline;
        }

        @media (max-width: 700px) {
            .container {
                width: 95%;
                margin: 20px auto;
            }

            .card {
                padding: 22px;
            }

            .grid {
                grid-template-columns: 1fr;
            }

            .full {
                grid-column: auto;
            }
        }
    </style>
</head>

<body>

<div class="container">
    <div class="card">

        <h1>Healthcare Professional Registration</h1>

        <p class="subtitle">
            Create your professional account. Your profile will require
            administrator verification before you can accept appointments.
        </p>

        <%-- Display registration success or error messages --%>
        <%
            String success = (String) request.getAttribute("success");
            String error = (String) request.getAttribute("error");
            Object userId = request.getAttribute("userId");
        %>

        <% if (success != null) { %>
            <div class="message success-message" role="status">
                <%= success %>

                <% if (userId != null) { %>
                    <br>
                    Your User ID is: <%= userId %>
                <% } %>
            </div>
        <% } %>

        <% if (error != null) { %>
            <div class="message error-message" role="alert">
                <%= error %>
            </div>
        <% } %>

        <form method="post"
              action="${pageContext.request.contextPath}/professional-register">

            <h2 class="section-title">Account Information</h2>

            <div class="grid">

                <div class="field">
                    <label for="fullName">Full Name</label>
                    <input
                        type="text"
                        id="fullName"
                        name="fullName"
                        required>
                </div>

                <div class="field">
                    <label for="email">Email</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        required>
                </div>

                <div class="field">
                    <label for="password">Password</label>
                    <input
                        type="password"
                        id="password"
                        name="password"
                        required>
                </div>

                <div class="field">
                    <label for="phone">Phone</label>
                    <input
                        type="tel"
                        id="phone"
                        name="phone"
                        required>
                </div>

            </div>

            <h2 class="section-title">Professional Information</h2>

            <div class="grid">

                <div class="field">
                    <label for="specializationId">Specialization</label>

                    <select
                        id="specializationId"
                        name="specializationId"
                        required>

                        <option value="">Select Specialization</option>
                        <option value="1">General Medicine</option>
                        <option value="2">Cardiology</option>
                        <option value="3">Dermatology</option>
                        <option value="4">Neurology</option>
                        <option value="5">Pediatrics</option>
                        <option value="6">Orthopedics</option>

                    </select>
                </div>

                <div class="field">
                    <label for="qualification">Qualification</label>
                    <input
                        type="text"
                        id="qualification"
                        name="qualification"
                        placeholder="e.g. MBBS, MD"
                        required>
                </div>

                <div class="field">
                    <label for="licenseNumber">License Number</label>
                    <input
                        type="text"
                        id="licenseNumber"
                        name="licenseNumber"
                        required>
                </div>

                <div class="field">
                    <label for="experienceYears">Experience (Years)</label>
                    <input
                        type="number"
                        id="experienceYears"
                        name="experienceYears"
                        min="0"
                        required>
                </div>

                <div class="field">
                    <label for="consultationFee">Consultation Fee (INR)</label>
                    <input
                        type="number"
                        id="consultationFee"
                        name="consultationFee"
                        min="0"
                        step="0.01"
                        required>
                </div>

                <div class="field full">
                    <label for="bio">Professional Bio</label>
                    <textarea
                        id="bio"
                        name="bio"
                        placeholder="Describe your professional experience and expertise..."></textarea>
                </div>

            </div>

            <button type="submit" class="button">
                Register as Healthcare Professional
            </button>

        </form>

        <div class="login-link">
            Already have an account?
            <a href="${pageContext.request.contextPath}/login">
                Login
            </a>
        </div>

    </div>
</div>

</body>
</html>