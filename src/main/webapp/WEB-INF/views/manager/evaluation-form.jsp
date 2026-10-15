<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="New Performance Evaluation — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h1 class="h2 fw-bold mb-1">New Performance Evaluation</h1>
            <p class="text-muted mb-0">Evaluate direct reports across active performance criteria with live score preview.</p>
        </div>
        <a href="${pageContext.request.contextPath}/manager/evaluations" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-arrow-left me-1"></i> Back to Evaluations
        </a>
    </div>
</div>

<c:choose>
    <c:when test="${empty criteria}">
        <div class="card epes-card shadow-sm p-5 text-center">
            <i class="bi bi-sliders fs-1 text-warning mb-3"></i>
            <h2 class="h5 fw-bold">Create criteria first</h2>
            <p class="text-muted mb-3">You must configure at least one active evaluation criterion before evaluating employees.</p>
            <div>
                <a href="${pageContext.request.contextPath}/manager/criteria" class="btn btn-primary">
                    <i class="bi bi-plus-circle me-1"></i> Configure Criteria
                </a>
            </div>
        </div>
    </c:when>

    <c:when test="${empty employees}">
        <div class="card epes-card shadow-sm p-5 text-center">
            <i class="bi bi-people fs-1 text-info mb-3"></i>
            <h2 class="h5 fw-bold">Add employees to a team first</h2>
            <p class="text-muted mb-3">You can only evaluate active employees who are members of teams that you manage.</p>
            <div>
                <a href="${pageContext.request.contextPath}/manager/teams" class="btn btn-primary">
                    <i class="bi bi-diagram-3 me-1"></i> Manage Teams
                </a>
            </div>
        </div>
    </c:when>

    <c:otherwise>
        <!-- Live Calculation Preview Header -->
        <div class="card bg-light border-primary shadow-sm mb-4" id="live-score-preview">
            <div class="card-body d-flex flex-wrap align-items-center justify-content-between gap-3">
                <div class="d-flex align-items-center gap-3">
                    <div class="fs-2 text-primary">
                        <i class="bi bi-rocket-takeoff-fill"></i>
                    </div>
                    <div>
                        <span class="small text-muted d-block text-uppercase fw-semibold">Projected Score & Stage</span>
                        <div class="d-flex align-items-center gap-2">
                            <span class="h3 mb-0 fw-bold" id="preview-score">--</span>
                            <span class="badge bg-secondary" id="preview-stage">None</span>
                        </div>
                    </div>
                </div>
                <div class="text-muted small">
                    <i class="bi bi-info-circle me-1"></i>
                    <span>Preview. Final score is calculated on save.</span>
                </div>
            </div>
        </div>

        <form id="evaluation-form" method="post" action="${pageContext.request.contextPath}/manager/evaluations/create">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>

            <div class="card epes-card shadow-sm mb-4">
                <div class="card-header bg-white py-3">
                    <h2 class="h5 fw-bold mb-0">1. Evaluation Details</h2>
                </div>
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-12 col-md-4">
                            <label for="employeeSelect" class="form-label fw-semibold">Employee <span class="text-danger">*</span></label>
                            <select id="employeeSelect" name="employeeId" class="form-select" required>
                                <option value="">Select Employee...</option>
                                <c:forEach var="emp" items="${employees}">
                                    <option value="${emp.id}" ${formEmployeeId == emp.id ? 'selected' : ''}>
                                        <c:out value="${emp.fullName}"/> (<c:out value="${emp.email}"/>)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-12 col-md-4">
                            <label for="teamSelect" class="form-label fw-semibold">Associated Team (Optional)</label>
                            <select id="teamSelect" name="teamId" class="form-select">
                                <option value="">No team</option>
                                <c:forEach var="t" items="${teams}">
                                    <option value="${t.id}" ${formTeamId == t.id ? 'selected' : ''}>
                                        <c:out value="${t.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-12 col-md-4">
                            <label for="periodInput" class="form-label fw-semibold">Evaluation Period <span class="text-danger">*</span></label>
                            <input type="text" id="periodInput" name="period" class="form-control"
                                   placeholder="Q3 2026" maxlength="100" required
                                   value="<c:out value='${formPeriod}'/>"/>
                        </div>

                        <div class="col-12">
                            <label for="summaryInput" class="form-label fw-semibold">Overall Summary Remarks</label>
                            <textarea id="summaryInput" name="summary" class="form-control" rows="3"
                                      placeholder="Provide qualitative feedback and strategic highlights for this cycle..."><c:out value='${formSummary}'/></textarea>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Criteria Scoring Blocks -->
            <div class="card epes-card shadow-sm mb-4">
                <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                    <h2 class="h5 fw-bold mb-0">2. Performance Criteria Scoring</h2>
                    <span class="badge bg-secondary-subtle text-secondary border">
                        <c:out value="${criteria.size()}"/> Active Criteria
                    </span>
                </div>
                <div class="card-body">
                    <p class="text-muted small mb-4">
                        Rate the employee on each relevant criterion. Unrated criteria are omitted. Criteria weights and names will be snapshotted.
                    </p>

                    <c:forEach var="c" items="${criteria}" varStatus="status">
                        <div class="criterion-block p-3 mb-3 border rounded bg-white shadow-xs"
                             data-criterion-id="${c.id}"
                             data-weight="${c.weight}">
                            <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-2">
                                <div>
                                    <h3 class="h6 fw-bold mb-1 text-primary">
                                        <c:out value="${c.name}"/>
                                    </h3>
                                    <p class="text-muted small mb-0">
                                        <c:out value="${c.description}"/>
                                    </p>
                                </div>
                                <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                                    Weight: <c:out value="${c.weight}"/>%
                                </span>
                            </div>

                            <div class="my-3">
                                <label class="form-label small fw-semibold text-muted d-block mb-2">Select Performance Rating:</label>
                                <div class="d-flex flex-wrap gap-2">
                                    <c:set var="selRating" value="${formRatings != null ? formRatings[c.id.toString()] : ''}" />

                                    <div class="form-check form-check-inline border rounded px-3 py-2 m-0">
                                        <input class="form-check-input" type="radio" name="rating_${c.id}" id="r1_${c.id}" value="1"
                                               ${selRating == '1' ? 'checked' : ''}>
                                        <label class="form-check-label small fw-medium" for="r1_${c.id}">1 Launchpad</label>
                                    </div>
                                    <div class="form-check form-check-inline border rounded px-3 py-2 m-0">
                                        <input class="form-check-input" type="radio" name="rating_${c.id}" id="r2_${c.id}" value="2"
                                               ${selRating == '2' ? 'checked' : ''}>
                                        <label class="form-check-label small fw-medium" for="r2_${c.id}">2 Liftoff</label>
                                    </div>
                                    <div class="form-check form-check-inline border rounded px-3 py-2 m-0">
                                        <input class="form-check-input" type="radio" name="rating_${c.id}" id="r3_${c.id}" value="3"
                                               ${selRating == '3' ? 'checked' : ''}>
                                        <label class="form-check-label small fw-medium" for="r3_${c.id}">3 Orbit</label>
                                    </div>
                                    <div class="form-check form-check-inline border rounded px-3 py-2 m-0">
                                        <input class="form-check-input" type="radio" name="rating_${c.id}" id="r4_${c.id}" value="4"
                                               ${selRating == '4' ? 'checked' : ''}>
                                        <label class="form-check-label small fw-medium" for="r4_${c.id}">4 Cruise</label>
                                    </div>
                                    <div class="form-check form-check-inline border rounded px-3 py-2 m-0">
                                        <input class="form-check-input" type="radio" name="rating_${c.id}" id="r5_${c.id}" value="5"
                                               ${selRating == '5' ? 'checked' : ''}>
                                        <label class="form-check-label small fw-medium" for="r5_${c.id}">5 Summit</label>
                                    </div>
                                </div>
                            </div>

                            <div class="mt-2">
                                <c:set var="selComment" value="${formComments != null ? formComments[c.id.toString()] : ''}" />
                                <input type="text" name="comment_${c.id}" class="form-control form-control-sm"
                                       placeholder="Specific observations or remarks for ${c.name} (optional)..."
                                       value="<c:out value='${selComment}'/>">
                            </div>
                        </div>
                    </c:forEach>
                </div>
                <div class="card-footer bg-light d-flex justify-content-end gap-2 py-3">
                    <a href="${pageContext.request.contextPath}/manager/evaluations" class="btn btn-outline-secondary">
                        Cancel
                    </a>
                    <button type="submit" class="btn btn-primary px-4">
                        <i class="bi bi-check-circle me-1"></i> Submit Evaluation
                    </button>
                </div>
            </div>
        </form>

        <script src="${pageContext.request.contextPath}/assets/js/evaluation-form.js"></script>
    </c:otherwise>
</c:choose>

<%@ include file="../fragments/footer.jspf" %>
