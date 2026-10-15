<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Performance Tracking — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom d-flex justify-content-between align-items-center">
    <div>
        <h1 class="h2 fw-bold mb-1">Performance Tracking</h1>
        <p class="text-muted mb-0">Track your overall score velocity and criterion breakdown across review periods.</p>
    </div>
    <a href="${pageContext.request.contextPath}/employee/dashboard" class="btn btn-outline-secondary btn-sm">
        <i class="bi bi-arrow-left me-1"></i> Dashboard
    </a>
</div>

<c:choose>
    <c:when test="${not hasEvaluations}">
        <div class="card epes-card shadow-sm p-5 text-center my-4">
            <i class="bi bi-clipboard-x fs-1 text-muted opacity-50 mb-3 d-block"></i>
            <h2 class="h5 fw-bold text-dark mb-1">No evaluations yet. Your manager will add them.</h2>
            <p class="text-muted small mb-0">Once evaluations are submitted by your team manager, your performance trajectory and criterion breakdowns will appear here.</p>
        </div>
    </c:when>
    <c:otherwise>
        <!-- Charts Row -->
        <div class="row g-4 mb-4">
            <!-- Score Progression Line Chart -->
            <div class="col-12 col-lg-7">
                <div class="card epes-card shadow-sm h-100">
                    <div class="card-header bg-white py-3">
                        <h2 class="h5 fw-bold mb-0">
                            <i class="bi bi-graph-up text-primary me-2"></i>Overall Score Progression
                        </h2>
                    </div>
                    <div class="card-body" style="min-height: 280px;">
                        <div style="height: 250px; position: relative;">
                            <canvas id="perfLineChart" data-chart="<c:out value='${lineChartJson}'/>"></canvas>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Latest Evaluation Criteria Bar Chart -->
            <div class="col-12 col-lg-5">
                <div class="card epes-card shadow-sm h-100">
                    <div class="card-header bg-white py-3">
                        <h2 class="h5 fw-bold mb-0">
                            <i class="bi bi-bar-chart text-info me-2"></i>Latest Review Ratings
                            <span class="badge bg-light text-muted border fw-normal ms-1">
                                <c:out value="${latestEvaluation.periodLabel}"/>
                            </span>
                        </h2>
                    </div>
                    <div class="card-body" style="min-height: 280px;">
                        <div style="height: 250px; position: relative;">
                            <canvas id="perfBarChart" data-chart="<c:out value='${barChartJson}'/>"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Evaluation History Table -->
        <div class="card epes-card shadow-sm mb-4">
            <div class="card-header bg-white py-3">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-clock-history text-secondary me-2"></i>Evaluation History
                </h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th scope="col" class="ps-3">Period</th>
                                <th scope="col">Manager</th>
                                <th scope="col">Overall Score</th>
                                <th scope="col">Rocket Stage</th>
                                <th scope="col">Team</th>
                                <th scope="col">Date</th>
                                <th scope="col" class="text-end pe-3">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="eval" items="${evaluations}">
                                <tr>
                                    <td class="ps-3 fw-semibold">
                                        <c:out value="${eval.periodLabel}"/>
                                    </td>
                                    <td>
                                        <c:out value="${managers[eval.managerId] != null ? managers[eval.managerId].fullName : 'Manager'}"/>
                                    </td>
                                    <td>
                                        <span class="fw-bold fs-6">
                                            <c:out value="${epesFmt:formatDecimal(eval.overallScore)}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <epes:stageBadge stage="${eval.overallStage}"/>
                                    </td>
                                    <td class="text-muted small">
                                        <c:out value="${teamNames[eval.teamId] != null ? teamNames[eval.teamId] : '-'}"/>
                                    </td>
                                    <td class="text-muted small">
                                        <c:out value="${epesFmt:formatDate(eval.createdAt)}"/>
                                    </td>
                                    <td class="text-end pe-3">
                                        <a href="${pageContext.request.contextPath}/employee/evaluations/view?id=${eval.id}"
                                           class="btn btn-sm btn-outline-primary">
                                            Open
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="../fragments/footer.jspf" %>
