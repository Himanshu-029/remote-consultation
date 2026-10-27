
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.remoteconsultation.model.ProfessionalDirectoryItem" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Find Professionals | Remote Consultation</title>

    <style>
        :root {
            --primary: #2563eb;
            --primary-dark: #1d4ed8;
            --background: #f4f7fb;
            --surface: #ffffff;
            --text: #172033;
            --muted: #64748b;
            --border: #e2e8f0;
            --success: #15803d;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, Helvetica, sans-serif;
            background: var(--background);
            color: var(--text);
        }

        header {
            background: var(--surface);
            border-bottom: 1px solid var(--border);
            padding: 18px 6%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
        }

        .brand {
            font-size: 21px;
            font-weight: 800;
            color: var(--primary);
        }

        header a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 600;
        }

        main {
            max-width: 1200px;
            margin: 0 auto;
            padding: 38px 22px 60px;
        }

        .hero {
            margin-bottom: 28px;
        }

        .hero h1 {
            margin: 0 0 10px;
            font-size: clamp(28px, 4vw, 38px);
        }

        .hero p {
            margin: 0;
            color: var(--muted);
            line-height: 1.6;
        }

        .toolbar {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            margin: 24px 0;
        }

        .toolbar input,
        .toolbar select {
            flex: 1;
            min-width: 220px;
            padding: 13px 14px;
            border: 1px solid var(--border);
            border-radius: 10px;
            background: white;
            font-size: 15px;
        }

        .results-count {
            color: var(--muted);
            margin-bottom: 16px;
        }

        .directory {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(270px, 1fr));
            gap: 20px;
        }

        .card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 23px;
            box-shadow: 0 5px 18px rgba(15, 23, 42, 0.04);
            display: flex;
            flex-direction: column;
            min-width: 0;
        }

        .avatar {
            width: 54px;
            height: 54px;
            border-radius: 15px;
            display: grid;
            place-items: center;
            background: #dbeafe;
            color: var(--primary-dark);
            font-size: 23px;
            font-weight: 800;
            margin-bottom: 17px;
        }

        .card h2 {
            margin: 0 0 7px;
            font-size: 20px;
            overflow-wrap: anywhere;
        }

        .specialty {
            color: var(--primary-dark);
            font-weight: 700;
            margin-bottom: 16px;
        }

        .detail {
            color: var(--muted);
            font-size: 14px;
            line-height: 1.6;
            margin: 5px 0;
        }

        .bio {
            margin: 13px 0;
            line-height: 1.6;
            overflow-wrap: anywhere;
        }

        .fee {
            margin-top: auto;
            padding-top: 18px;
            font-size: 22px;
            font-weight: 800;
        }

        .fee small {
            font-size: 13px;
            font-weight: 400;
            color: var(--muted);
        }

        .book-button {
            display: block;
            text-align: center;
            background: var(--primary);
            color: white;
            padding: 12px;
            margin-top: 16px;
            border-radius: 9px;
            text-decoration: none;
            font-weight: 700;
        }

        .book-button:hover {
            background: var(--primary-dark);
        }

        .empty {
            background: white;
            border: 1px dashed var(--border);
            border-radius: 14px;
            padding: 35px;
            text-align: center;
            color: var(--muted);
            grid-column: 1 / -1;
        }

        .hidden {
            display: none !important;
        }

        @media (max-width: 600px) {
            header {
                padding: 16px 20px;
            }

            main {
                padding: 28px 16px 45px;
            }
        }
    </style>
</head>

<body>
<header>
    <div class="brand">RemoteCare</div>
    <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
</header>

<main>
    <section class="hero">
        <h1>Find the right professional</h1>
        <p>Explore verified healthcare professionals and compare their qualifications, experience, and consultation fees.</p>
    </section>

    <div class="toolbar">
        <input
            id="searchInput"
            type="search"
            placeholder="Search by name or qualification"
            aria-label="Search professionals">

        <select id="specializationFilter" aria-label="Filter by specialization">
            <option value="">All specializations</option>
            <c:forEach var="professional" items="${professionals}">
                <option value="<c:out value='${professional.specializationName}'/>">
                    <c:out value="${professional.specializationName}"/>
                </option>
            </c:forEach>
        </select>
    </div>

    <div class="results-count" id="resultsCount"></div>

    <section class="directory" id="directory">
        <c:forEach var="professional" items="${professionals}">
            <article class="card"
                     data-name="<c:out value='${professional.fullName}'/>"
                     data-specialty="<c:out value='${professional.specializationName}'/>"
                     data-qualification="<c:out value='${professional.qualification}'/>">

                <div class="avatar" aria-hidden="true">
                    <c:out value="${professional.fullName.substring(0,1)}"/>
                </div>

                <h2>
                    <c:out value="${professional.fullName}"/>
                </h2>

                <div class="specialty">
                    <c:out value="${professional.specializationName}"/>
                </div>

                <p class="detail">
                    <strong>Qualification:</strong>
                    <c:out value="${professional.qualification}"/>
                </p>

                <p class="detail">
                    <strong>Experience:</strong>
                    <c:choose>
                        <c:when test="${professional.experienceYears != null}">
                            <c:out value="${professional.experienceYears}"/> years
                        </c:when>
                        <c:otherwise>Not specified</c:otherwise>
                    </c:choose>
                </p>

                <c:if test="${not empty professional.bio}">
                    <p class="bio"><c:out value="${professional.bio}"/></p>
                </c:if>

                <div class="fee">
                    ₹<c:out value="${professional.consultationFee}"/>
                    <small>/ consultation</small>
                </div>


            <a class="book-button"
                href="${pageContext.request.contextPath}/patient/professional-availability?professionalId=${professional.professionalId}">
                View availability
            </a>

            </article>
        </c:forEach>

        <div class="empty" id="emptyState"
             <c:if test="${not empty professionals}">style="display:none"</c:if>>
            No approved professionals are available yet.
        </div>
    </section>
</main>

<script>
    const searchInput = document.getElementById("searchInput");
    const specializationFilter =
        document.getElementById("specializationFilter");
    const cards = [...document.querySelectorAll(".card")];
    const emptyState = document.getElementById("emptyState");
    const resultsCount = document.getElementById("resultsCount");

    // Remove duplicate specializations from the filter.
    const options = [...specializationFilter.options];
    const seen = new Set();

    options.forEach((option, index) => {
        if (index === 0) return;

        const key = option.value.trim().toLowerCase();

        if (seen.has(key)) {
            option.remove();
        } else {
            seen.add(key);
        }
    });

    function filterProfessionals() {
        const query = searchInput.value.trim().toLowerCase();
        const specialty = specializationFilter.value.toLowerCase();
        let visible = 0;

        cards.forEach(card => {
            const searchableText = [
                card.dataset.name,
                card.dataset.specialty,
                card.dataset.qualification
            ].join(" ").toLowerCase();

            const matchesSearch = searchableText.includes(query);
            const matchesSpecialty =
                !specialty ||
                card.dataset.specialty.toLowerCase() === specialty;

            const show = matchesSearch && matchesSpecialty;

            card.classList.toggle("hidden", !show);

            if (show) visible++;
        });

        resultsCount.textContent =
            visible + (visible === 1
                ? " professional found"
                : " professionals found");

        emptyState.style.display = visible === 0 ? "block" : "none";
    }

    searchInput.addEventListener("input", filterProfessionals);
    specializationFilter.addEventListener("change", filterProfessionals);

    filterProfessionals();
</script>
</body>
</html>
