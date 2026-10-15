<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Performance Report — ${report.employee.fullName} — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom d-flex flex-wrap justify-content-between align-items-center gap-2">
    <div>
        <h1 class="h2 fw-bold mb-1">
            Performance Report &mdash; <c:out value="${report.employee.fullName}"/>
        </h1>
        <p class="text-muted mb-0">
            Self-assessment analytics, longitudinal performance trends, and goal progression.
        </p>
    </div>
    <div class="d-flex gap-2">
        <button type="button" class="btn btn-outline-dark btn-sm" onclick="window.print()">
            <i class="bi bi-printer me-1"></i> Print this page
        </button>
        <c:choose>
            <c:when test="${not empty backUrl}">
                <a href="<c:out value='${backUrl}'/>" class="btn btn-outline-secondary btn-sm">
                    <i class="bi bi-arrow-left me-1"></i> <c:out value="${backLabel != null ? backLabel : 'Back'}"/>
                </a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/employee/dashboard" class="btn btn-outline-secondary btn-sm">
                    <i class="bi bi-arrow-left me-1"></i> Dashboard
                </a>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- 6 KPI Cards -->
<div class="row g-3 mb-4">
    <!-- Average Score -->
    <div class="col-6 col-md-4 col-lg-2">
        <div class="card epes-card p-3 shadow-sm h-100 text-center">
            <span class="text-muted small fw-semibold text-uppercase mb-1">Average Score</span>
            <div class="h3 fw-bold text-primary mb-0">
                <c:choose>
                    <c:when test="${hasEvaluations}">
                        <c:out value="${epesFmt:formatDecimal(report.averageScore)}"/>
                    </c:when>
                    <c:otherwise>-</c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small">Lifetime mean</span>
        </div>
    </div>

    <!-- Latest Score -->
    <div class="col-6 col-md-4 col-lg-2">
        <div class="card epes-card p-3 shadow-sm h-100 text-center">
            <span class="text-muted small fw-semibold text-uppercase mb-1">Latest Score</span>
            <div class="h3 fw-bold text-info mb-0">
                <c:choose>
                    <c:when test="${hasEvaluations}">
                        <c:out value="${epesFmt:formatDecimal(report.latestScore)}"/>
                    </c:when>
                    <c:otherwise>-</c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small">Most recent</span>
        </div>
    </div>

    <!-- Current Stage -->
    <div class="col-6 col-md-4 col-lg-2">
        <div class="card epes-card p-3 shadow-sm h-100 text-center">
            <span class="text-muted small fw-semibold text-uppercase mb-1">Current Stage</span>
            <div class="mb-1 mt-1">
                <c:choose>
                    <c:when test="${hasEvaluations}">
                        <epes:stageBadge stage="${report.latestStage}"/>
                    </c:when>
                    <c:otherwise><span class="text-muted">-</span></c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small">Tier level</span>
        </div>
    </div>

    <!-- Trend -->
    <div class="col-6 col-md-4 col-lg-2">
        <div class="card epes-card p-3 shadow-sm h-100 text-center">
            <span class="text-muted small fw-semibold text-uppercase mb-1">Trajectory</span>
            <div class="h5 fw-bold mb-1 mt-1">
                <c:choose>
                    <c:when test="${hasEvaluations}">
                        <c:choose>
                            <c:when test="${report.trend == 'IMPROVING'}">
                                <span class="text-success"><i class="bi bi-arrow-up-right me-1"></i>Improving</span>
                            </c:when>
                            <c:when test="${report.trend == 'DECLINING'}">
                                <span class="text-danger"><i class="bi bi-arrow-down-right me-1"></i>Declining</span>
                            </c:when>
                            <c:when test="${report.trend == 'STABLE'}">
                                <span class="text-primary"><i class="bi bi-arrow-right me-1"></i>Stable</span>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted small">Not enough data</span>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise><span class="text-muted">-</span></c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small">Velocity</span>
        </div>
    </div>

    <!-- Evaluations Count -->
    <div class="col-6 col-md-4 col-lg-2">
        <div class="card epes-card p-3 shadow-sm h-100 text-center">
            <span class="text-muted small fw-semibold text-uppercase mb-1">Evaluations</span>
            <div class="h3 fw-bold text-dark mb-0">
                <c:out value="${report.evaluations.size()}"/>
            </div>
            <span class="text-muted small">Total reviews</span>
        </div>
    </div>

    <!-- Feedback Average -->
    <div class="col-6 col-md-4 col-lg-2">
        <div class="card epes-card p-3 shadow-sm h-100 text-center">
            <span class="text-muted small fw-semibold text-uppercase mb-1">Feedback Avg</span>
            <div class="h3 fw-bold text-warning mb-0">
                <c:choose>
                    <c:when test="${report.feedbackCount > 0}">
                        <c:out value="${epesFmt:formatDecimal(report.averageFeedbackRating)}"/>
                    </c:when>
                    <c:otherwise>-</c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small"><c:out value="${report.feedbackCount}"/> reviews</span>
        </div>
    </div>
</div>

<!-- Charts Row 1: Line Chart & Doughnut Chart -->
<div class="row g-4 mb-4">
    <!-- Line Chart: Score Progression -->
    <div class="col-12 col-lg-8">
        <div class="card epes-card shadow-sm h-100">
            <div class="card-header bg-white py-3">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-graph-up text-primary me-2"></i>Performance Score Progression
                </h2>
            </div>
            <div class="card-body" style="min-height: 280px;">
                <div style="height: 250px; position: relative;">
                    <canvas id="reportLineChart" data-chart="<c:out value='${lineChartJson}'/>"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Doughnut Chart: Goal Status Breakdown -->
    <div class="col-12 col-lg-4">
        <div class="card epes-card shadow-sm h-100">
            <div class="card-header bg-white py-3">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-pie-chart text-success me-2"></i>Goal Status Breakdown
                </h2>
            </div>
            <div class="card-body" style="min-height: 280px;">
                <div style="height: 250px; position: relative;">
                    <canvas id="reportGoalChart" data-chart="<c:out value='${goalDoughnutJson}'/>"></canvas>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Charts Row 2: Bar Chart: Average Rating per Criterion -->
<div class="row g-4 mb-4">
    <div class="col-12">
        <div class="card epes-card shadow-sm">
            <div class="card-header bg-white py-3">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-bar-chart-steps text-info me-2"></i>Average Rating per Criterion (Across All Reviews)
                </h2>
            </div>
            <div class="card-body" style="min-height: 280px;">
                <div style="height: 250px; position: relative;">
                    <canvas id="reportCritChart" data-chart="<c:out value='${critBarJson}'/>"></canvas>
                </div>
                <c:if test="${not empty report.averageRatingByCriterion}">
                    <div class="table-responsive mt-3">
                        <table class="table table-sm table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th scope="col" class="ps-3">Criterion Name</th>
                                    <th scope="col" class="text-end pe-3">Average Rating</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${report.averageRatingByCriterion}">
                                    <tr>
                                        <td class="ps-3 fw-semibold"><c:out value="${entry.key}"/></td>
                                        <td class="text-end pe-3">
                                            <span class="fw-bold"><c:out value="${epesFmt:formatDecimal(entry.value)}"/></span> / 5.00
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>

<%@ include file="../fragments/footer.jspf" %>
