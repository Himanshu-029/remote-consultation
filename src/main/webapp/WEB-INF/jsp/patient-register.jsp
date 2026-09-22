<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Patient Registration | Remote Consultation</title>

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
            max-width: 850px;
            margin: 40px auto;
            background: #ffffff;
            padding: 35px;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        h1 {
            margin-top: 0;
            text-align: center;
            color: #2563eb;
        }

        .subtitle {
            text-align: center;
            color: #6b7280;
            margin-bottom: 30px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .form-group {
            display: flex;
            flex-direction: column;
        }

        .full-width {
            grid-column: 1 / -1;
        }

        label {
            margin-bottom: 7px;
            font-weight: bold;
        }

        input,
        select,
        textarea {
            padding: 11px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            font-size: 15px;
            width: 100%;
        }

        textarea {
            min-height: 90px;
            resize: vertical;
        }

        input:focus,
        select:focus,
        textarea:focus {
            outline: none;
            border-color: #2563eb;
        }

        .submit-btn {
            width: 100%;
            padding: 13px;
            margin-top: 25px;
            border: none;
            border-radius: 7px;
            background: #2563eb;
            color: white;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
        }

        .submit-btn:hover {
            background: #1d4ed8;
        }

        .login-link {
            text-align: center;
            margin-top: 20px;
        }

        .login-link a {
            color: #2563eb;
            text-decoration: none;
            font-weight: bold;
        }

        @media (max-width: 650px) {
            .form-grid {
                grid-template-columns: 1fr;
            }

            .full-width {
                grid-column: auto;
            }

            .container {
                width: 95%;
                padding: 25px;
            }
        }
    </style>
</head>

<body>

<div class="container">

    <h1>Patient Registration</h1>

    <p class="subtitle">
        Create your account to consult healthcare professionals remotely.
    </p>

    <form action="${pageContext.request.contextPath}/register"
          method="post">

        <div class="form-grid">

            <div class="form-group">
                <label for="fullName">Full Name</label>
                <input
                        type="text"
                        id="fullName"
                        name="fullName"
                        required>
            </div>

            <div class="form-group">
                <label for="email">Email</label>
                <input
                        type="email"
                        id="email"
                        name="email"
                        required>
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input
                        type="password"
                        id="password"
                        name="password"
                        required
                        minlength="8">
            </div>

            <div class="form-group">
                <label for="phone">Phone Number</label>
                <input
                        type="tel"
                        id="phone"
                        name="phone"
                        required>
            </div>

            <div class="form-group">
                <label for="dateOfBirth">Date of Birth</label>
                <input
                        type="date"
                        id="dateOfBirth"
                        name="dateOfBirth">
            </div>

            <div class="form-group">
                <label for="gender">Gender</label>
                <select id="gender" name="gender">
                    <option value="">Select Gender</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                </select>
            </div>

            <div class="form-group">
                <label for="bloodGroup">Blood Group</label>
                <select id="bloodGroup" name="bloodGroup">
                    <option value="">Select Blood Group</option>
                    <option value="A+">A+</option>
                    <option value="A-">A-</option>
                    <option value="B+">B+</option>
                    <option value="B-">B-</option>
                    <option value="AB+">AB+</option>
                    <option value="AB-">AB-</option>
                    <option value="O+">O+</option>
                    <option value="O-">O-</option>
                </select>
            </div>

            <div class="form-group">
                <label for="emergencyContact">Emergency Contact</label>
                <input
                        type="tel"
                        id="emergencyContact"
                        name="emergencyContact">
            </div>

            <div class="form-group full-width">
                <label for="address">Address</label>
                <textarea
                        id="address"
                        name="address"
                        placeholder="Enter your address"></textarea>
            </div>

        </div>

        <button type="submit" class="submit-btn">
            Create Patient Account
        </button>

    </form>

    <div class="login-link">
        Already have an account?
        <a href="${pageContext.request.contextPath}/login">
            Login
        </a>
    </div>

</div>

</body>
</html>