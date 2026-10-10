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
                <span>Team Leadership & Evaluation &mdash; Rocket Stage: <strong class="text-primary">Orbit / Cruise</strong></span>
            </p>
        </div>
    </div>
</div>

<div class="row g-4">
    <!-- Performance Evaluation -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-primary">
                        <i class="bi bi-clipboard-check"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
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

    <!-- Goal Setting -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-info">
                        <i class="bi bi-bullseye"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
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

    <!-- Feedback Provision -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-success">
                        <i class="bi bi-chat-square-quote"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
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

    <!-- Performance Reports -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-warning">
                        <i class="bi bi-graph-up-arrow"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
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

    <!-- Team Management -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-dark">
                        <i class="bi bi-diagram-3"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
                </div>
                <h2 class="h5 fw-bold card-title">Team Management</h2>
                <p class="card-text text-muted small">
                    View assigned teams, manage team membership, and allocate team-level objectives.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>Manage Teams</button>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
