
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Professional Verification | Admin</title>

    <style>
        :root {
            color-scheme: light;
            --primary: #2457d6;
            --success: #18794e;
            --danger: #b42318;
            --ink: #172033;
            --muted: #64748b;
            --border: #e2e8f0;
            --surface: #ffffff;
            --background: #f4f7fb;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, Helvetica, sans-serif;
            color: var(--ink);
            background: var(--background);
        }

        header {
            background: #101b35;
            color: white;
            padding: 22px 5%;
        }

        header h1 {
            margin: 0;
            font-size: 24px;
        }

        header p {
            margin: 7px 0 0;
            color: #cbd5e1;
        }

        main {
            width: min(1200px, 94%);
            margin: 32px auto;
        }

        .intro {
            margin-bottom: 24px;
        }

        .intro h2 {
            margin-bottom: 8px;
            font-size: 27px;
        }

        .muted {
            color: var(--muted);
        }

        .notice {
            padding: 13px 16px;
            margin: 14px 0;
            border-radius: 8px;
            background: #eaf1ff;
            color: #1e40af;
        }

        .notice.success {
            background: #e8f7ee;
            color: #166534;
        }

        .notice.error {
            background: #fff0ef;
            color: #991b1b;
        }

        .summary {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            margin-bottom: 18px;
            flex-wrap: wrap;
        }

        .count {
            background: white;
            border: 1px solid var(--border);
            border-radius: 10px;
            padding: 12px 16px;
        }

        .professional-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 14px;
            padding: 22px;
            margin-bottom: 18px;
            box-shadow: 0 5px 18px rgba(15, 23, 42, .04);
        }

        .card-header {
            display: flex;
            justify-content: space-between;
            gap: 14px;
            flex-wrap: wrap;
            border-bottom: 1px solid var(--border);
            padding-bottom: 14px;
            margin-bottom: 16px;
        }

        .card-header h3 {
            margin: 0 0 7px;
            font-size: 20px;
        }

        .badge {
            align-self: flex-start;
            background: #fff5d6;
            color: #854d0e;
            padding: 6px 10px;
            border-radius: 30px;
            font-size: 12px;
            font-weight: bold;
        }

        .details {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 18px;
        }

        .detail-label {
            display: block;
            color: var(--muted);
            font-size: 12px;
            margin-bottom: 5px;
        }

        .detail-value {
            overflow-wrap: anywhere;
            line-height: 1.5;
        }

        .bio {
            margin-top: 18px;
            padding: 14px;
            background: #f8fafc;
            border-radius: 8px;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }

        .bio p {
            margin-bottom: 0;
        }

        .actions {
            display: flex;
            gap: 10px;
            margin-top: 20px;
            padding-top: 16px;
            border-top: 1px solid var(--border);
        }

        .actions form {
            margin: 0;
        }

        button {
            border: 0;
            border-radius: 8px;
            padding: 11px 18px;
            color: white;
            font-weight: bold;
            cursor: pointer;
        }

        button:focus-visible {
            outline: 3px solid #93c5fd;
            outline-offset: 3px;
        }

        button:disabled {
            opacity: .6;
            cursor: not-allowed;
        }

        .approve {
            background: var(--success);
        }

        .reject {
            background: var(--danger);
        }

        .empty {
            padding: 40px 20px;
            text-align: center;
            background: white;
            border: 1px solid var(--border);
            border-radius: 14px;
        }

        @media (max-width: 650px) {
            .details {
                grid-template-columns: 1fr;
            }

            .professional-card {
                padding: 16px;
            }

            .actions {
                flex-direction: column;
            }

            .actions form,
            button {
                width: 100%;
            }
        }
    </style>
</head>

<body>
<header>
    <h1>Remote Consultation</h1>
    <p>Administrator workspace - Professional verification</p>
</header>

<main>
    <section class="intro">
        <h2>Professional verification queue</h2>
        <p class="muted">
            Review credentials carefully before approving a professional
            to provide consultations.
        </p>
    </section>

    <c:if test="${param.result == 'approved'}">
        <div class="notice success" role="status">
            Professional approved successfully.
        </div>
    </c:if>

    <c:if test="${param.result == 'rejected'}">
        <div class="notice error" role="status">
            Professional application rejected.
        </div>
    </c:if>

    <c:if test="${param.result == 'stale'}">
        <div class="notice error" role="alert">
            This profile was already reviewed or is no longer pending.
            Refresh the queue and try again if necessary.
        </div>
    </c:if>

    <c:set var="pendingCount"
           value="${empty pendingProfessionals ? 0 : pendingProfessionals.size()}" />

    <div class="summary">
        <div>
            <strong>Pending applications</strong>
            <p class="muted">Review each application before taking action.</p>
        </div>

        <div class="count">
            Awaiting review:
            <strong><c:out value="${pendingCount}" /></strong>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty pendingProfessionals}">
            <div class="empty">
                <h3>You're all caught up</h3>
                <p class="muted">
                    There are currently no pending professional applications.
                </p>
            </div>
        </c:when>

        <c:otherwise>
            <c:forEach var="professional"
                       items="${pendingProfessionals}">

                <article class="professional-card">
                    <div class="card-header">
                        <div>
                            <h3>
                                <c:out value="${professional.fullName}"
                                       default="Name not provided" />
                            </h3>

                            <div class="muted">
                                <c:out value="${professional.email}"
                                       default="Email not provided" />
                            </div>
                        </div>

                        <span class="badge">PENDING REVIEW</span>
                    </div>

                    <div class="details">
                        <div>
                            <span class="detail-label">Specialization</span>
                            <div class="detail-value">
                                <c:out value="${professional.specializationName}"
                                       default="Not provided" />
                            </div>
                        </div>

                        <div>
                            <span class="detail-label">Qualification</span>
                            <div class="detail-value">
                                <c:out value="${professional.qualification}"
                                       default="Not provided" />
                            </div>
                        </div>

                        <div>
                            <span class="detail-label">Professional license</span>
                            <div class="detail-value">
                                <c:out value="${professional.licenseNumber}"
                                       default="Not provided" />
                            </div>
                        </div>

                        <div>
                            <span class="detail-label">Experience</span>
                            <div class="detail-value">
                                <c:choose>
                                    <c:when test="${professional.experienceYears != null}">
                                        <c:out value="${professional.experienceYears}" />
                                        years
                                    </c:when>
                                    <c:otherwise>
                                        Not provided
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div>
                            <span class="detail-label">Consultation fee</span>
                            <div class="detail-value">
                                <c:choose>
                                    <c:when test="${professional.consultationFee != null}">
                                        &#8377;<c:out value="${professional.consultationFee}" />
                                    </c:when>
                                    <c:otherwise>
                                        Not provided
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div>
                            <span class="detail-label">Application submitted</span>
                            <div class="detail-value">
                                <c:out value="${professional.createdAt}"
                                       default="Not available" />
                            </div>
                        </div>
                    </div>

                    <div class="bio">
                        <strong>Professional bio</strong>
                        <p>
                            <c:out value="${professional.bio}"
                                   default="No biography provided." />
                        </p>
                    </div>

                    <div class="actions">
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/verification"
                              onsubmit="return confirm('Approve this professional?');">

                            <input type="hidden"
                                   name="_csrf"
                                   value="<c:out value='${csrfToken}' />">

                            <input type="hidden"
                                   name="professionalId"
                                   value="<c:out value='${professional.professionalId}' />">

                            <input type="hidden"
                                   name="decision"
                                   value="APPROVED">

                            <button class="approve" type="submit">
                                Approve professional
                            </button>
                        </form>

                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/verification"
                              onsubmit="return confirm('Reject this professional?');">

                            <input type="hidden"
                                   name="_csrf"
                                   value="<c:out value='${csrfToken}' />">

                            <input type="hidden"
                                   name="professionalId"
                                   value="<c:out value='${professional.professionalId}' />">

                            <input type="hidden"
                                   name="decision"
                                   value="REJECTED">

                            <button class="reject" type="submit">
                                Reject professional
                            </button>
                        </form>
                    </div>
                </article>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
