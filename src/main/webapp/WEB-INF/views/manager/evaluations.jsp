<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Performance Evaluations — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom">
    <div>
        <h1 class="h2 fw-bold mb-1">Performance Evaluations</h1>
        <p class="text-muted mb-0">Assess employee achievements, score performance, and calculate rocket stage ratings.</p>
    </div>
    <div>
        <a href="${pageContext.request.contextPath}/manager/evaluations/new" class="btn btn-primary">
            <i class="bi bi-plus-circle me-1"></i> New Evaluation
        </a>
    </div>
</div>

<div class="card epes-card shadow-sm">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="ps-3">Employee</th>
                        <th scope="col">Period</th>
                        <th scope="col">Score</th>
                        <th scope="col">Stage</th>
                        <th scope="col">Team</th>
                        <th scope="col">Date</th>
                        <th scope="col" class="text-end pe-3">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty evaluations}">
                            <tr>
                                <td colspan="7" class="text-center py-5 text-muted">
                                    <i class="bi bi-clipboard-x fs-1 d-block mb-2 text-secondary"></i>
                                    <p class="mb-2 fw-semibold">No evaluations yet.</p>
                                    <p class="small text-muted mb-3">Conduct your first performance evaluation for a team member.</p>
                                    <a href="${pageContext.request.contextPath}/manager/evaluations/new" class="btn btn-sm btn-outline-primary">
                                        Create Evaluation
                                    </a>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="eval" items="${evaluations}">
                                <tr>
                                    <td class="ps-3 fw-semibold">
                                        <c:out value="${people[eval.employeeId] != null ? people[eval.employeeId].fullName : 'Unknown user'}"/>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border">
                                            <c:out value="${eval.periodLabel}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="fw-bold">
                                            <fmt:formatNumber value="${eval.overallScore}" minFractionDigits="2" maxFractionDigits="2"/>
                                        </span>
                                    </td>
                                    <td>
                                        <epes:stageBadge stage="${eval.overallStage}"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty eval.teamId and not empty teamNames[eval.teamId]}">
                                                <c:out value="${teamNames[eval.teamId]}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">-</span>
                                            </c:otherwise>
                                        </c:choose>
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

<%@ include file="../fragments/footer.jspf" %>
