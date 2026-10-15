<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Feedback History — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom d-flex justify-content-between align-items-center">
    <div>
        <h1 class="h2 fw-bold mb-1">Feedback History</h1>
        <p class="text-muted mb-0">Review supervisory feedback, ratings, and recognition across review cycles.</p>
    </div>
    <a href="${pageContext.request.contextPath}/employee/dashboard" class="btn btn-outline-secondary btn-sm">
        <i class="bi bi-arrow-left me-1"></i> Dashboard
    </a>
</div>

<!-- Average Rating KPI Card -->
<div class="row g-4 mb-4">
    <div class="col-12 col-md-4">
        <div class="card epes-card shadow-sm p-4">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small fw-semibold text-uppercase">Average Feedback Rating</span>
                <i class="bi bi-star-fill text-warning fs-4"></i>
            </div>
            <div class="display-5 fw-bold text-dark mb-1">
                <c:choose>
                    <c:when test="${not empty feedbackList}">
                        <c:out value="${epesFmt:formatDecimal(averageRating)}"/>
                        <span class="fs-5 text-muted fw-normal">/ 5.00</span>
                    </c:when>
                    <c:otherwise>
                        <span class="fs-4 text-muted">-</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <span class="text-muted small">Based on <c:out value="${feedbackList.size()}"/> feedback reviews</span>
        </div>
    </div>
</div>

<c:choose>
    <c:when test="${empty feedbackList}">
        <div class="card epes-card shadow-sm p-5 text-center my-4">
            <i class="bi bi-chat-square-dots fs-1 text-muted opacity-50 mb-3 d-block"></i>
            <h2 class="h5 fw-bold text-dark mb-1">No feedback yet.</h2>
            <p class="text-muted small mb-0">When your supervisors submit performance feedback and recommendations, they will appear here.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="card epes-card shadow-sm">
            <div class="card-header bg-white py-3">
                <h2 class="h5 fw-bold mb-0">All Feedback Entries</h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th scope="col" class="ps-3">Manager</th>
                                <th scope="col">Rating</th>
                                <th scope="col">Comments</th>
                                <th scope="col">Linked Period</th>
                                <th scope="col" class="text-end pe-3">Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fb" items="${feedbackList}">
                                <tr>
                                    <td class="ps-3 fw-semibold">
                                        <c:out value="${managers[fb.managerId] != null ? managers[fb.managerId].fullName : 'Manager'}"/>
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
                                    <td style="max-width: 450px;">
                                        <span class="text-dark"><c:out value="${fb.comments}"/></span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty fb.evaluationId and not empty evalPeriods[fb.evaluationId]}">
                                                <span class="badge bg-light text-dark border">
                                                    <c:out value="${evalPeriods[fb.evaluationId]}"/>
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">-</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end pe-3 small text-muted">
                                        <c:out value="${epesFmt:formatDateTime(fb.createdAt)}"/>
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
