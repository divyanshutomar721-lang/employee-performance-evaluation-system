<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty sessionScope.currentUser}">
    <c:redirect url="/home" />
</c:if>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EPES — Employee Performance Evaluation System</title>
    <meta name="description"
          content="Employee Performance Evaluation System — track goals, feedback and performance reviews.">

    <!-- Bootstrap 5 CSS (Local) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendor/bootstrap.min.css">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"
          rel="stylesheet">
    <!-- Google Font: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap"
          rel="stylesheet">

    <style>
        :root {
            --epes-primary: #6366f1;
            --epes-primary-dark: #4f46e5;
            --epes-bg: #0f172a;
            --epes-surface: #1e293b;
            --epes-text: #e2e8f0;
            --epes-muted: #94a3b8;
            --epes-accent: #22d3ee;
        }

        * { box-sizing: border-box; }

        body {
            font-family: 'Inter', sans-serif;
            background: var(--epes-bg);
            color: var(--epes-text);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            margin: 0;
        }

        .hero {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            text-align: center;
            padding: 3rem 1rem;
            background: linear-gradient(135deg, #0f172a 0%, #1e293b 40%, #312e81 100%);
            position: relative;
            overflow: hidden;
        }

        .hero::before {
            content: '';
            position: absolute;
            width: 600px;
            height: 600px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(99,102,241,.15) 0%, transparent 70%);
            top: -200px;
            right: -150px;
            animation: pulse 6s ease-in-out infinite;
        }

        @keyframes pulse {
            0%, 100% { transform: scale(1); opacity: .6; }
            50%      { transform: scale(1.15); opacity: 1; }
        }

        .hero-card {
            background: rgba(30, 41, 59, .75);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(99,102,241,.25);
            border-radius: 1.25rem;
            padding: 3rem 2.5rem;
            max-width: 560px;
            width: 100%;
            box-shadow: 0 25px 50px -12px rgba(0,0,0,.5);
            animation: fadeUp .8s ease-out;
        }

        @keyframes fadeUp {
            from { opacity: 0; transform: translateY(30px); }
            to   { opacity: 1; transform: translateY(0); }
        }

        .hero-card h1 {
            font-weight: 700;
            font-size: 2rem;
            margin-bottom: .75rem;
            background: linear-gradient(90deg, var(--epes-primary), var(--epes-accent));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .hero-card p {
            color: var(--epes-muted);
            font-size: 1.05rem;
            margin-bottom: 2rem;
            line-height: 1.6;
        }

        .btn-epes {
            background: var(--epes-primary);
            color: #fff;
            border: none;
            padding: .65rem 1.75rem;
            border-radius: .5rem;
            font-weight: 600;
            font-size: .95rem;
            text-decoration: none;
            transition: background .2s, transform .15s, box-shadow .2s;
        }

        .btn-epes:hover {
            background: var(--epes-primary-dark);
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(99,102,241,.35);
            color: #fff;
        }

        .btn-outline-epes {
            background: transparent;
            color: var(--epes-accent);
            border: 1px solid var(--epes-accent);
            padding: .65rem 1.75rem;
            border-radius: .5rem;
            font-weight: 600;
            font-size: .95rem;
            text-decoration: none;
            transition: background .2s, transform .15s;
        }

        .btn-outline-epes:hover {
            background: rgba(34,211,238,.1);
            transform: translateY(-2px);
            color: var(--epes-accent);
        }

        footer {
            text-align: center;
            padding: 1.25rem;
            color: var(--epes-muted);
            font-size: .85rem;
            border-top: 1px solid rgba(148,163,184,.15);
        }
    </style>
</head>
<body>

<section class="hero">
    <div class="hero-card">
        <h1>Employee Performance Evaluation System</h1>
        <p>
            Track goals, share feedback, and drive performance &mdash;
            all in one place. Built for managers and employees.
        </p>
        <div class="d-flex justify-content-center gap-3 flex-wrap">
            <a href="${pageContext.request.contextPath}/login"
               class="btn-epes">
                <i class="bi bi-box-arrow-in-right me-1"></i> Sign in
            </a>
            <a href="${pageContext.request.contextPath}/health"
               class="btn-outline-epes">System Health</a>
        </div>
    </div>
</section>

<footer>
    &copy; 2026 EPES &mdash; College Project
</footer>

<!-- Bootstrap 5 JS bundle (Local) -->
<script src="${pageContext.request.contextPath}/assets/vendor/bootstrap.bundle.min.js"></script>
</body>
</html>
