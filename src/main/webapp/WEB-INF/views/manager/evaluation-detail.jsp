<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Evaluation Details — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
            <h1 class="h2 fw-bold mb-1">Performance Evaluation Details</h1>
            <p class="text-muted mb-0">
                Evaluation for <strong class="text-dark"><c:out value="${people[evaluation.employeeId] != null ? people[evaluation.employeeId].fullName : 'Unknown user'}"/></strong>
                &bull; Period: <span class="badge bg-light text-dark border"><c:out value="${evaluation.periodLabel}"/></span>
            </p>
        </div>
        <div class="d-flex gap-2">
            <a href="${pageContext.request.contextPath}/manager/evaluations" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left me-1"></i> Back to Evaluations
            </a>
            <form method="post" action="${pageContext.request.contextPath}/manager/evaluations/delete"
                  onsubmit="return confirm('Are you sure you want to permanently delete this evaluation?');" class="d-inline">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                <input type="hidden" name="id" value="${evaluation.id}"/>
                <button type="submit" class="btn btn-outline-danger">
                    <i class="bi bi-trash me-1"></i> Delete Evaluation
                </button>
            </form>
        </div>
    </div>
</div>

<div class="row g-4 mb-4">
    <!-- Score & Stage Card -->
    <div class="col-12 col-md-5">
        <div class="card epes-card shadow-sm h-100 border-primary">
            <div class="card-body d-flex flex-column justify-content-center align-items-center text-center p-4">
                <span class="small text-muted text-uppercase fw-semibold mb-2">Overall Performance Score</span>
                <div class="display-3 fw-bold text-primary mb-2">
                    <fmt:formatNumber value="${evaluation.overallScore}" minFractionDigits="2" maxFractionDigits="2"/>
                </div>
                <div class="mb-3">
                    <epes:stageBadge stage="${evaluation.overallStage}"/>
                </div>
                <p class="text-muted small mb-0">
                    Calculated using weighted performance formula across all snapshotted criteria.
                </p>
            </div>
        </div>
    </div>

    <!-- Metadata Card -->
    <div class="col-12 col-md-7">
        <div class="card epes-card shadow-sm h-100">
            <div class="card-header bg-white py-3">
                <h2 class="h6 fw-bold mb-0">Evaluation Information</h2>
            </div>
            <div class="card-body">
                <dl class="row mb-0">
                    <dt class="col-sm-4 text-muted">Evaluated Employee</dt>
                    <dd class="col-sm-8 fw-semibold">
                        <c:out value="${people[evaluation.employeeId] != null ? people[evaluation.employeeId].fullName : 'Unknown user'}"/>
                        <span class="text-muted fw-normal small">(<c:out value="${people[evaluation.employeeId] != null ? people[evaluation.employeeId].email : '-'}"/>)</span>
                    </dd>

                    <dt class="col-sm-4 text-muted">Evaluating Manager</dt>
                    <dd class="col-sm-8">
                        <c:out value="${people[evaluation.managerId] != null ? people[evaluation.managerId].fullName : 'Unknown user'}"/>
                    </dd>

                    <dt class="col-sm-4 text-muted">Team</dt>
                    <dd class="col-sm-8">
                        <c:out value="${teamName}"/>
                    </dd>

                    <dt class="col-sm-4 text-muted">Evaluation Period</dt>
                    <dd class="col-sm-8">
                        <span class="badge bg-light text-dark border">
                            <c:out value="${evaluation.periodLabel}"/>
                        </span>
                    </dd>

                    <dt class="col-sm-4 text-muted">Conducted Date</dt>
                    <dd class="col-sm-8">
                        <c:out value="${epesFmt:formatDateTime(evaluation.createdAt)}"/>
                    </dd>

                    <dt class="col-sm-4 text-muted">Summary Comments</dt>
                    <dd class="col-sm-8">
                        <c:choose>
                            <c:when test="${not empty evaluation.summaryComment}">
                                <div class="p-2 bg-light rounded text-dark small">
                                    <c:out value="${evaluation.summaryComment}"/>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted fst-italic">No general comments provided.</span>
                            </c:otherwise>
                        </c:choose>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
</div>

<!-- Criterion Snapshot Scores Table -->
<div class="card epes-card shadow-sm">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <h2 class="h5 fw-bold mb-0">Criteria Snapshot Breakdown</h2>
        <span class="badge bg-light text-muted border">
            <c:out value="${evaluation.scores.size()}"/> Criteria Evaluated
        </span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="ps-3">Criterion Snapshot Name</th>
                        <th scope="col">Weight</th>
                        <th scope="col">Rating</th>
                        <th scope="col">Stage Level</th>
                        <th scope="col" class="pe-3">Specific Comment</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="score" items="${evaluation.scores}">
                        <tr>
                            <td class="ps-3 fw-semibold">
                                <c:out value="${score.criterionName}"/>
                            </td>
                            <td>
                                <span class="badge bg-light text-dark border">
                                    <c:out value="${score.weight}"/>%
                                </span>
                            </td>
                            <td>
                                <span class="fw-bold"><c:out value="${score.rating}"/></span> / 5
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${score.rating == 1}"><span class="badge stage-launchpad badge-stage-launchpad">Launchpad</span></c:when>
                                    <c:when test="${score.rating == 2}"><span class="badge stage-liftoff badge-stage-liftoff">Liftoff</span></c:when>
                                    <c:when test="${score.rating == 3}"><span class="badge stage-orbit badge-stage-orbit">Orbit</span></c:when>
                                    <c:when test="${score.rating == 4}"><span class="badge stage-cruise badge-stage-cruise">Cruise</span></c:when>
                                    <c:when test="${score.rating == 5}"><span class="badge stage-summit badge-stage-summit">Summit</span></c:when>
                                    <c:otherwise><span class="badge bg-secondary">-</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="pe-3">
                                <c:choose>
                                    <c:when test="${not empty score.comment}">
                                        <c:out value="${score.comment}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted">-</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="../fragments/footer.jspf" %>
