<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Performance Reports — Manager — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom d-flex flex-wrap justify-content-between align-items-center gap-2">
    <div>
        <h1 class="h2 fw-bold mb-1">Team Performance Reports</h1>
        <p class="text-muted mb-0">Overview of managed teams, member performance rollups, and longitudinal reports.</p>
    </div>
    <a href="${pageContext.request.contextPath}/manager/dashboard" class="btn btn-outline-secondary btn-sm">
        <i class="bi bi-arrow-left me-1"></i> Dashboard
    </a>
</div>

<!-- Overview Totals KPI Cards -->
<div class="row g-4 mb-4">
    <div class="col-12 col-md-4">
        <div class="card epes-card shadow-sm p-3">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small fw-semibold text-uppercase">Managed Teams</span>
                <i class="bi bi-diagram-3 text-primary fs-4"></i>
            </div>
            <div class="display-6 fw-bold text-dark mb-1">
                <c:out value="${overview.teams.size()}"/>
            </div>
            <span class="text-muted small">Active departmental units</span>
        </div>
    </div>

    <div class="col-12 col-md-4">
        <div class="card epes-card shadow-sm p-3">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small fw-semibold text-uppercase">Assigned Employees</span>
                <i class="bi bi-people text-info fs-4"></i>
            </div>
            <div class="display-6 fw-bold text-dark mb-1">
                <c:out value="${overview.totalEmployees}"/>
            </div>
            <span class="text-muted small">Distinct supervised team members</span>
        </div>
    </div>

    <div class="col-12 col-md-4">
        <div class="card epes-card shadow-sm p-3">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small fw-semibold text-uppercase">Overall Average Score</span>
                <i class="bi bi-speedometer2 text-success fs-4"></i>
            </div>
            <div class="display-6 fw-bold text-success mb-1">
                <c:choose>
                    <c:when test="${overview.overallAverage > 0}">
                        <c:out value="${epesFmt:formatDecimal(overview.overallAverage)}"/>
                    </c:when>
                    <c:otherwise>
                        <span class="fs-4 text-muted">-</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small">Weighted mean across evaluated teams</span>
        </div>
    </div>
</div>

<!-- Managed Teams Table -->
<c:choose>
    <c:when test="${empty overview.teams}">
        <div class="card epes-card shadow-sm p-5 text-center my-4">
            <i class="bi bi-diagram-3 fs-1 text-muted opacity-50 mb-3 d-block"></i>
            <h2 class="h5 fw-bold text-dark mb-1">No teams managed yet.</h2>
            <p class="text-muted small mb-3">Create and configure your teams first to view aggregate performance reports.</p>
            <div>
                <a href="${pageContext.request.contextPath}/manager/teams" class="btn btn-sm btn-primary">
                    <i class="bi bi-plus-circle me-1"></i> Manage Teams
                </a>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="card epes-card shadow-sm">
            <div class="card-header bg-white py-3">
                <h2 class="h5 fw-bold mb-0">Managed Teams Overview</h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th scope="col" class="ps-3">Team Name</th>
                                <th scope="col">Description</th>
                                <th scope="col">Members</th>
                                <th scope="col">Team Average Score</th>
                                <th scope="col" class="text-end pe-3">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tr" items="${overview.teams}">
                                <tr>
                                    <td class="ps-3 fw-semibold">
                                        <c:out value="${tr.team.name}"/>
                                    </td>
                                    <td class="text-muted">
                                        <c:out value="${tr.team.description}"/>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border">
                                            <c:out value="${tr.team.members.size()}"/> members
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${tr.teamAverageScore > 0}">
                                                <span class="fw-bold fs-6">
                                                    <c:out value="${epesFmt:formatDecimal(tr.teamAverageScore)}"/>
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted fst-italic">No evaluations</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end pe-3">
                                        <a href="${pageContext.request.contextPath}/manager/reports/team?id=${tr.team.id}"
                                           class="btn btn-sm btn-outline-primary">
                                            Open report <i class="bi bi-arrow-right ms-1"></i>
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
