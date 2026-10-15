<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Team Report — ${teamReport.team.name} — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom d-flex flex-wrap justify-content-between align-items-center gap-2">
    <div>
        <h1 class="h2 fw-bold mb-1">
            Team Report &mdash; <c:out value="${teamReport.team.name}"/>
        </h1>
        <p class="text-muted mb-0">
            <span class="badge bg-primary-subtle text-primary border border-primary-subtle me-2">
                <c:out value="${teamReport.team.members.size()}"/> Team Members
            </span>
            <span>Performance distribution, stage velocity, and individual employee rankings.</span>
        </p>
    </div>
    <div class="d-flex gap-2">
        <button type="button" class="btn btn-outline-dark btn-sm" onclick="window.print()">
            <i class="bi bi-printer me-1"></i> Print Report
        </button>
        <a href="${pageContext.request.contextPath}/manager/reports" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-arrow-left me-1"></i> Back to Reports
        </a>
    </div>
</div>

<!-- Team Metrics and Stage Distribution Row -->
<div class="row g-4 mb-4">
    <!-- Team Average Score Card -->
    <div class="col-12 col-lg-4">
        <div class="card epes-card shadow-sm h-100 p-4 d-flex flex-column justify-content-center text-center">
            <span class="text-muted small fw-semibold text-uppercase mb-2">Team Average Performance</span>
            <div class="display-3 fw-bold text-primary mb-2">
                <c:choose>
                    <c:when test="${teamReport.teamAverageScore > 0}">
                        <c:out value="${epesFmt:formatDecimal(teamReport.teamAverageScore)}"/>
                    </c:when>
                    <c:otherwise>
                        <span class="fs-2 text-muted">-</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <p class="text-muted small mb-0">
                Composite weighted mean calculated across all evaluated team members.
            </p>
        </div>
    </div>

    <!-- Stage Distribution Bar Chart -->
    <div class="col-12 col-lg-8">
        <div class="card epes-card shadow-sm h-100">
            <div class="card-header bg-white py-3">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-bar-chart-line text-info me-2"></i>Rocket Stage Distribution
                </h2>
            </div>
            <div class="card-body" style="min-height: 280px;">
                <div style="height: 250px; position: relative;">
                    <canvas id="teamStageChart" data-chart="<c:out value='${stageChartJson}'/>"></canvas>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Ranked Employee Performance Table -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <h2 class="h5 fw-bold mb-0">Ranked Team Member Performance</h2>
        <span class="badge bg-light text-muted border">
            <c:out value="${teamReport.rows.size()}"/> Members Ranked
        </span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="ps-3">#</th>
                        <th scope="col">Employee Name</th>
                        <th scope="col">Average Score</th>
                        <th scope="col">Latest Stage</th>
                        <th scope="col">Goal Completion</th>
                        <th scope="col" class="text-end pe-3">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty teamReport.rows}">
                            <tr>
                                <td colspan="6" class="text-center py-4 text-muted">
                                    No members currently assigned to this team.
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="row" items="${teamReport.rows}" varStatus="status">
                                <tr>
                                    <td class="ps-3 text-muted small fw-semibold">
                                        <c:out value="${status.index + 1}"/>
                                    </td>
                                    <td class="fw-semibold">
                                        <c:out value="${row.employeeName}"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${row.hasEvaluations()}">
                                                <span class="fw-bold fs-6">
                                                    <c:out value="${epesFmt:formatDecimal(row.averageScore)}"/>
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="text-muted fst-italic">No evaluations</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${row.hasEvaluations()}">
                                                <epes:stageBadge stage="${row.latestStage}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">-</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center gap-2" style="max-width: 160px;">
                                            <div class="progress flex-grow-1" style="height: 6px;">
                                                <div class="progress-bar bg-success" style="width: ${row.goalCompletionPercent}%;"></div>
                                            </div>
                                            <span class="small text-muted fw-semibold">${row.goalCompletionPercent}%</span>
                                        </div>
                                    </td>
                                    <td class="text-end pe-3">
                                        <a href="${pageContext.request.contextPath}/manager/reports/employee?id=${row.employeeId}&teamId=${teamReport.team.id}"
                                           class="btn btn-sm btn-outline-primary">
                                            Employee report <i class="bi bi-arrow-right ms-1"></i>
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

<%@ include file="../fragments/footer.jspf" %>
