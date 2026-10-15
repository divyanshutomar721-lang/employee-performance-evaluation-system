<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
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
                        <c:when test="${hasEvaluations}">
                            <epes:stageBadge stage="${latestStage}"/>
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

<!-- KPI Summary Highlights -->
<div class="row g-3 mb-4">
    <div class="col-12 col-sm-6 col-lg-3">
        <div class="card epes-card p-3 shadow-sm h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small fw-semibold">Current Stage</span>
                <i class="bi bi-rocket-takeoff text-primary fs-4"></i>
            </div>
            <div class="h3 fw-bold mb-1">
                <c:choose>
                    <c:when test="${hasEvaluations}">
                        <epes:stageBadge stage="${latestStage}"/>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted fs-6">No evaluations yet</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small">Progression tier</span>
        </div>
    </div>

    <div class="col-12 col-sm-6 col-lg-3">
        <div class="card epes-card p-3 shadow-sm h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small fw-semibold">Latest Score</span>
                <i class="bi bi-speedometer2 text-info fs-4"></i>
            </div>
            <div class="h3 fw-bold mb-1">
                <c:choose>
                    <c:when test="${hasEvaluations}">
                        <c:out value="${epesFmt:formatDecimal(latestScore)}"/>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted fs-6">No evaluations yet</span>
                    </c:otherwise>

                </c:choose>
            </div>
            <span class="text-muted small">Most recent review score</span>
        </div>
    </div>

    <div class="col-12 col-sm-6 col-lg-3">
        <div class="card epes-card p-3 shadow-sm h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small fw-semibold">Trajectory Trend</span>
                <i class="bi bi-graph-up-arrow text-success fs-4"></i>
            </div>
            <div class="h4 fw-bold mb-1">
                <c:choose>
                    <c:when test="${hasEvaluations}">
                        <c:choose>
                            <c:when test="${trend == 'IMPROVING'}">
                                <span class="text-success"><i class="bi bi-arrow-up-right me-1"></i>Improving</span>
                            </c:when>
                            <c:when test="${trend == 'DECLINING'}">
                                <span class="text-danger"><i class="bi bi-arrow-down-right me-1"></i>Declining</span>
                            </c:when>
                            <c:when test="${trend == 'STABLE'}">
                                <span class="text-primary"><i class="bi bi-arrow-right me-1"></i>Stable</span>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">Not enough data</span>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted fs-6">No evaluations yet</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small">Period-over-period direction</span>
        </div>
    </div>

    <div class="col-12 col-sm-6 col-lg-3">
        <div class="card epes-card p-3 shadow-sm h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small fw-semibold">Goal Status</span>
                <i class="bi bi-flag text-warning fs-4"></i>
            </div>
            <div class="d-flex gap-2 flex-wrap align-items-center mb-1">
                <span class="badge bg-success" title="Completed"><c:out value="${goalCounts['COMPLETED'] != null ? goalCounts['COMPLETED'] : 0}"/> Done</span>
                <span class="badge bg-primary" title="In Progress"><c:out value="${goalCounts['IN_PROGRESS'] != null ? goalCounts['IN_PROGRESS'] : 0}"/> Active</span>
                <span class="badge bg-secondary" title="Not Started"><c:out value="${goalCounts['NOT_STARTED'] != null ? goalCounts['NOT_STARTED'] : 0}"/> New</span>
            </div>
            <span class="text-muted small">Personal & assigned goals</span>
        </div>
    </div>
</div>

<!-- Quick Navigation Cards -->
<div class="row g-4 mb-4">
    <!-- Performance Tracking -->
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card epes-card h-100 p-3 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-primary">
                        <i class="bi bi-speedometer2"></i>
                    </div>
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle">History</span>
                </div>
                <h2 class="h5 fw-bold card-title">Performance</h2>
                <p class="card-text text-muted small">
                    Track your current evaluation scores, rocket stage progression, and historical reviews.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <a href="${pageContext.request.contextPath}/employee/performance" class="btn btn-sm btn-primary w-100">
                    <i class="bi bi-arrow-right-circle me-1"></i> View Performance
                </a>
            </div>
        </div>
    </div>

    <!-- Feedback History -->
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card epes-card h-100 p-3 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-info">
                        <i class="bi bi-chat-heart"></i>
                    </div>
                    <span class="badge bg-info-subtle text-info border border-info-subtle">Feedback</span>
                </div>
                <h2 class="h5 fw-bold card-title">Feedback</h2>
                <p class="card-text text-muted small">
                    Review praise, constructive feedback, and comments submitted by your managers.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <a href="${pageContext.request.contextPath}/employee/feedback" class="btn btn-sm btn-info text-white w-100">
                    <i class="bi bi-arrow-right-circle me-1"></i> View Feedback
                </a>
            </div>
        </div>
    </div>

    <!-- Personal Goals -->
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card epes-card h-100 p-3 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-success">
                        <i class="bi bi-flag"></i>
                    </div>
                    <span class="badge bg-success-subtle text-success border border-success-subtle">Goals</span>
                </div>
                <h2 class="h5 fw-bold card-title">Goals</h2>
                <p class="card-text text-muted small">
                    Create personal development goals, update your milestones, and monitor deadlines.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <a href="${pageContext.request.contextPath}/employee/goals" class="btn btn-sm btn-success w-100">
                    <i class="bi bi-arrow-right-circle me-1"></i> Manage Goals
                </a>
            </div>
        </div>
    </div>

    <!-- Performance Reports -->
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card epes-card h-100 p-3 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-warning">
                        <i class="bi bi-file-earmark-bar-graph"></i>
                    </div>
                    <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Reports</span>
                </div>
                <h2 class="h5 fw-bold card-title">Reports</h2>
                <p class="card-text text-muted small">
                    Self-assessment summary, criterion radar analytics, and print-ready reports.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <a href="${pageContext.request.contextPath}/employee/reports" class="btn btn-sm btn-warning text-dark w-100">
                    <i class="bi bi-arrow-right-circle me-1"></i> View Reports
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Progress Chart & Recent Feedback Row -->
<div class="row g-4">
    <!-- Score Progression Chart -->
    <div class="col-12 col-lg-7">
        <div class="card epes-card shadow-sm h-100">
            <div class="card-header bg-white py-3">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-graph-up text-primary me-2"></i>Performance Score Progression
                </h2>
            </div>
            <div class="card-body d-flex flex-column justify-content-center" style="min-height: 280px;">
                <c:choose>
                    <c:when test="${hasEvaluations}">
                        <div style="height: 250px; position: relative;">
                            <canvas id="employeeScoreChart" data-chart="<c:out value='${lineChartJson}'/>"></canvas>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-clipboard-x fs-1 mb-2 d-block text-secondary opacity-50"></i>
                            <p class="mb-0 fw-semibold">No evaluations yet.</p>
                            <p class="small text-muted mb-0">Your manager will conduct evaluations for your assigned team.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Recent Feedback -->
    <div class="col-12 col-lg-5">
        <div class="card epes-card shadow-sm h-100">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-chat-heart text-info me-2"></i>Recent Feedback
                </h2>
                <a href="${pageContext.request.contextPath}/employee/feedback" class="btn btn-sm btn-link p-0 text-decoration-none">
                    View all <i class="bi bi-arrow-right"></i>
                </a>
            </div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${empty latestFeedback}">
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-chat-square-dots fs-1 mb-2 d-block text-secondary opacity-50"></i>
                            <p class="mb-0 fw-semibold">No feedback yet.</p>
                            <p class="small text-muted mb-0">Feedback from your supervisors will appear here.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <ul class="list-group list-group-flush">
                            <c:forEach var="fb" items="${latestFeedback}">
                                <li class="list-group-item p-3">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <span class="fw-semibold text-dark">
                                            <c:out value="${managers[fb.managerId] != null ? managers[fb.managerId].fullName : 'Manager'}"/>
                                        </span>
                                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                                            <c:out value="${fb.rating}"/> / 5
                                        </span>
                                    </div>
                                    <p class="mb-1 text-muted small text-truncate">
                                        <c:out value="${fb.comments}"/>
                                    </p>
                                    <small class="text-muted">
                                        <c:out value="${epesFmt:formatDateTime(fb.createdAt)}"/>
                                    </small>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
