<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Employee Dashboard — EPES" />
<%@ include file="fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
            <h1 class="h2 fw-bold mb-1">
                Welcome, <c:out value="${user.fullName}"/>
            </h1>
            <p class="text-muted mb-0">
                <span class="badge bg-success me-2">EMPLOYEE</span>
                <span>Individual Growth & Performance &mdash; Rocket Stage:
                    <c:choose>
                        <c:when test="${not empty rocketStage}">
                            <strong class="text-primary"><c:out value="${rocketStage}"/></strong>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted fst-italic">No evaluations yet</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </p>
        </div>
    </div>
</div>

<div class="row g-4">
    <!-- Performance Tracking -->
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-primary">
                        <i class="bi bi-speedometer2"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
                </div>
                <h2 class="h5 fw-bold card-title">Performance Tracking</h2>
                <p class="card-text text-muted small">
                    Track your current evaluation scores, rocket stage progression, and historical trends over time.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>View Progress</button>
            </div>
        </div>
    </div>

    <!-- Feedback History -->
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-info">
                        <i class="bi bi-chat-dots"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
                </div>
                <h2 class="h5 fw-bold card-title">Feedback History</h2>
                <p class="card-text text-muted small">
                    Review praise, constructive feedback, and comments submitted by managers and peers.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>View Feedback</button>
            </div>
        </div>
    </div>

    <!-- Personal Goals -->
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-success">
                        <i class="bi bi-check2-circle"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
                </div>
                <h2 class="h5 fw-bold card-title">Personal Goals</h2>
                <p class="card-text text-muted small">
                    Set personal professional development goals, update milestones, and mark items completed.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>Manage Goals</button>
            </div>
        </div>
    </div>

    <!-- Performance Reports -->
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-warning">
                        <i class="bi bi-file-earmark-bar-graph"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
                </div>
                <h2 class="h5 fw-bold card-title">Performance Reports</h2>
                <p class="card-text text-muted small">
                    Generate and download individual performance summary sheets and evaluation history.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>Download Report</button>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
