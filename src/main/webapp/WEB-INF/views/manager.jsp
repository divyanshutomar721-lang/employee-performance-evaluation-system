<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manager Dashboard — EPES" />
<%@ include file="fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
            <h1 class="h2 fw-bold mb-1">
                Welcome, <c:out value="${user.fullName}"/>
            </h1>
            <p class="text-muted mb-0">
                <span class="badge bg-primary me-2">MANAGER</span>
                <span>Team Leadership & Evaluation</span>
            </p>
        </div>
    </div>
</div>

<div class="row g-4">
    <!-- Team Management (Enabled) -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card h-100 p-3 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-primary">
                        <i class="bi bi-diagram-3-fill"></i>
                    </div>
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                        <c:out value="${teamCount}"/> Teams
                    </span>
                </div>
                <h2 class="h5 fw-bold card-title">Team Management</h2>
                <p class="card-text text-muted small mb-3">
                    Organize departmental teams, manage member rosters, and assign team-level responsibilities.
                </p>
                <div class="small text-muted bg-light p-2 rounded mb-2">
                    <div class="d-flex justify-content-between">
                        <span>Managed Teams: <strong><c:out value="${teamCount}"/></strong></span>
                        <span>Assigned Employees: <strong><c:out value="${employeeCount}"/></strong></span>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <a href="${pageContext.request.contextPath}/manager/teams" class="btn btn-sm btn-primary w-100">
                    <i class="bi bi-arrow-right-circle me-1"></i> Manage Teams
                </a>
            </div>
        </div>
    </div>

    <!-- Evaluation Criteria (Enabled) -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card h-100 p-3 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-success">
                        <i class="bi bi-sliders"></i>
                    </div>
                    <span class="badge bg-success-subtle text-success border border-success-subtle">
                        <c:out value="${criteriaCount}"/> Criteria
                    </span>
                </div>
                <h2 class="h5 fw-bold card-title">Evaluation Criteria</h2>
                <p class="card-text text-muted small mb-3">
                    Configure weighted performance criteria for reviews and evaluation scoring.
                </p>
                <div class="small text-muted bg-light p-2 rounded mb-2">
                    <div class="d-flex justify-content-between">
                        <span>Configured Criteria: <strong><c:out value="${criteriaCount}"/></strong></span>
                        <span class="text-success fw-semibold">Active Weights</span>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <a href="${pageContext.request.contextPath}/manager/criteria" class="btn btn-sm btn-success w-100">
                    <i class="bi bi-arrow-right-circle me-1"></i> Manage Criteria
                </a>
            </div>
        </div>
    </div>

    <!-- Performance Evaluation (Coming in Phase 4B) -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-secondary">
                        <i class="bi bi-clipboard-check"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4B</span>
                </div>
                <h2 class="h5 fw-bold card-title">Performance Evaluation</h2>
                <p class="card-text text-muted small">
                    Conduct weighted criterion evaluations, score performance, and calculate rocket stage ratings for direct reports.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>Start Evaluation</button>
            </div>
        </div>
    </div>

    <!-- Goal Setting (Coming in Phase 4B) -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-secondary">
                        <i class="bi bi-bullseye"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4B</span>
                </div>
                <h2 class="h5 fw-bold card-title">Goal Setting</h2>
                <p class="card-text text-muted small">
                    Assign organizational and team goals to employees with target completion dates and milestone tracking.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>Manage Goals</button>
            </div>
        </div>
    </div>

    <!-- Feedback Provision (Coming in Phase 4B) -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-secondary">
                        <i class="bi bi-chat-square-quote"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4B</span>
                </div>
                <h2 class="h5 fw-bold card-title">Feedback Provision</h2>
                <p class="card-text text-muted small">
                    Provide continuous, constructive feedback and quarterly praise to team members.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>Give Feedback</button>
            </div>
        </div>
    </div>

    <!-- Performance Reports (Coming in Phase 4B) -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-secondary">
                        <i class="bi bi-graph-up-arrow"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4B</span>
                </div>
                <h2 class="h5 fw-bold card-title">Performance Reports</h2>
                <p class="card-text text-muted small">
                    View team score distributions, stage velocity, and export asynchronous CSV/PDF summary reports.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>View Reports</button>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
