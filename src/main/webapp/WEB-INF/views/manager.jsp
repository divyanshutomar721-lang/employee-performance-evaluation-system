<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
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
                <span>Team Leadership, Performance Evaluation & Goal Tracking</span>
            </p>
        </div>
    </div>
</div>

<!-- Overview Cards -->
<div class="row g-4 mb-5">
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

    <!-- Performance Reports (Enabled) -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card h-100 p-3 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-warning">
                        <i class="bi bi-file-earmark-bar-graph"></i>
                    </div>
                    <span class="badge bg-warning-subtle text-warning border border-warning-subtle">
                        Analytics
                    </span>
                </div>
                <h2 class="h5 fw-bold card-title">Performance Reports</h2>
                <p class="card-text text-muted small mb-3">
                    View team score distributions, stage velocity, and individual employee analytics.
                </p>
                <div class="small text-muted bg-light p-2 rounded mb-2">
                    <div class="d-flex justify-content-between">
                        <span>Team Rollups</span>
                        <span class="text-primary fw-semibold">Stage Velocity</span>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <a href="${pageContext.request.contextPath}/manager/reports" class="btn btn-sm btn-warning text-dark w-100">
                    <i class="bi bi-arrow-right-circle me-1"></i> View Reports
                </a>
            </div>
        </div>
    </div>
</div>

<c:if test="${not empty stageChartJson}">
    <div class="card epes-card shadow-sm mb-4">
        <div class="card-header bg-white py-3">
            <h2 class="h5 fw-bold mb-0">
                <i class="bi bi-bar-chart-line text-info me-2"></i>Overall Team Stage Distribution
            </h2>
        </div>
        <div class="card-body" style="min-height: 240px;">
            <div style="height: 200px; position: relative;">
                <canvas id="managerOverviewStageChart" data-chart="<c:out value='${stageChartJson}'/>"></canvas>
            </div>
        </div>
    </div>
</c:if>


<!-- Section 1: Performance Evaluation -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
            <i class="bi bi-clipboard-check fs-5 text-primary"></i>
            <h2 class="h5 fw-bold mb-0">Performance Evaluation</h2>
        </div>
        <div class="d-flex gap-2">
            <a href="${pageContext.request.contextPath}/manager/evaluations/new" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-plus-circle me-1"></i> New Evaluation
            </a>
            <a href="${pageContext.request.contextPath}/manager/evaluations" class="btn btn-sm btn-primary">
                View all <i class="bi bi-arrow-right ms-1"></i>
            </a>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="ps-3">Employee</th>
                        <th scope="col">Period</th>
                        <th scope="col">Score</th>
                        <th scope="col">Stage</th>
                        <th scope="col">Date</th>
                        <th scope="col" class="text-end pe-3">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty recentEvaluations}">
                            <tr>
                                <td colspan="6" class="text-center py-4 text-muted">
                                    <p class="mb-1 fw-semibold">No evaluations yet.</p>
                                    <a href="${pageContext.request.contextPath}/manager/evaluations/new" class="small">Conduct your first evaluation</a>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="eval" items="${recentEvaluations}">
                                <tr>
                                    <td class="ps-3 fw-semibold">
                                        <c:out value="${people[eval.employeeId] != null ? people[eval.employeeId].fullName : 'Unknown user'}"/>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border"><c:out value="${eval.periodLabel}"/></span>
                                    </td>
                                    <td>
                                        <span class="fw-bold">
                                            <fmt:formatNumber value="${eval.overallScore}" minFractionDigits="2" maxFractionDigits="2"/>
                                        </span>
                                    </td>
                                    <td>
                                        <epes:stageBadge stage="${eval.overallStage}"/>
                                    </td>
                                    <td class="small text-muted">
                                        <c:out value="${epesFmt:formatDate(eval.createdAt)}"/>
                                    </td>
                                    <td class="text-end pe-3">
                                        <a href="${pageContext.request.contextPath}/manager/evaluations/view?id=${eval.id}"
                                           class="btn btn-sm btn-outline-primary">
                                            Open
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Section 2: Goal Setting -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
            <i class="bi bi-flag fs-5 text-primary"></i>
            <h2 class="h5 fw-bold mb-0">Goal Setting</h2>
        </div>
        <a href="${pageContext.request.contextPath}/manager/goals" class="btn btn-sm btn-primary">
            View all <i class="bi bi-arrow-right ms-1"></i>
        </a>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="ps-3">Employee</th>
                        <th scope="col">Description</th>
                        <th scope="col">Target Date</th>
                        <th scope="col">Status</th>
                        <th scope="col" class="pe-3">Progress</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty recentGoals}">
                            <tr>
                                <td colspan="5" class="text-center py-4 text-muted">
                                    <p class="mb-1 fw-semibold">No goals assigned yet.</p>
                                    <a href="${pageContext.request.contextPath}/manager/goals" class="small">Assign a new goal</a>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="goal" items="${recentGoals}">
                                <c:set var="isOverdue" value="${goal.targetDate.isBefore(today) and goal.status != 'COMPLETED'}"/>
                                <tr>
                                    <td class="ps-3 fw-semibold">
                                        <c:out value="${people[goal.employeeId] != null ? people[goal.employeeId].fullName : 'Unknown user'}"/>
                                    </td>
                                    <td>
                                        <span class="text-dark"><c:out value="${goal.description}"/></span>
                                    </td>
                                    <td>
                                        <span class="${isOverdue ? 'text-danger fw-bold' : ''}">
                                            <c:out value="${epesFmt:formatDate(goal.targetDate)}"/>
                                        </span>
                                        <c:if test="${isOverdue}">
                                            <span class="badge bg-danger ms-1">Overdue</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <epes:statusBadge status="${goal.status}"/>
                                    </td>
                                    <td class="pe-3">
                                        <div class="d-flex align-items-center gap-2" style="min-width: 140px;">
                                            <div class="progress flex-grow-1" style="height: 6px;">
                                                <div class="progress-bar ${goal.progressPercent == 100 ? 'bg-success' : 'bg-primary'}"
                                                     style="width: ${goal.progressPercent}%;"></div>
                                            </div>
                                            <span class="small fw-semibold text-muted">${goal.progressPercent}%</span>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Section 3: Feedback Provision -->
<div class="card epes-card shadow-sm">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
            <i class="bi bi-chat-heart fs-5 text-primary"></i>
            <h2 class="h5 fw-bold mb-0">Feedback Provision</h2>
        </div>
        <a href="${pageContext.request.contextPath}/manager/feedback" class="btn btn-sm btn-primary">
            View all <i class="bi bi-arrow-right ms-1"></i>
        </a>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="ps-3">Employee</th>
                        <th scope="col">Rating</th>
                        <th scope="col">Comments</th>
                        <th scope="col" class="text-end pe-3">Date</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty recentFeedback}">
                            <tr>
                                <td colspan="4" class="text-center py-4 text-muted">
                                    <p class="mb-1 fw-semibold">No feedback given yet.</p>
                                    <a href="${pageContext.request.contextPath}/manager/feedback" class="small">Share feedback with a team member</a>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="fb" items="${recentFeedback}">
                                <tr>
                                    <td class="ps-3 fw-semibold">
                                        <c:out value="${people[fb.employeeId] != null ? people[fb.employeeId].fullName : 'Unknown user'}"/>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <span class="fw-bold"><c:out value="${fb.rating}"/>/5</span>
                                            <c:choose>
                                                <c:when test="${fb.rating == 1}"><span class="badge stage-launchpad badge-stage-launchpad">Launchpad</span></c:when>
                                                <c:when test="${fb.rating == 2}"><span class="badge stage-liftoff badge-stage-liftoff">Liftoff</span></c:when>
                                                <c:when test="${fb.rating == 3}"><span class="badge stage-orbit badge-stage-orbit">Orbit</span></c:when>
                                                <c:when test="${fb.rating == 4}"><span class="badge stage-cruise badge-stage-cruise">Cruise</span></c:when>
                                                <c:when test="${fb.rating == 5}"><span class="badge stage-summit badge-stage-summit">Summit</span></c:when>
                                                <c:otherwise><span class="badge bg-secondary">-</span></c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="text-dark text-truncate d-inline-block" style="max-width: 400px;">
                                            <c:out value="${fb.comments}"/>
                                        </span>
                                    </td>
                                    <td class="text-end pe-3 small text-muted">
                                        <c:out value="${epesFmt:formatDateTime(fb.createdAt)}"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
