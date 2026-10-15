<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Continuous Feedback — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h1 class="h2 fw-bold mb-1">Continuous Feedback</h1>
            <p class="text-muted mb-0">Provide real-time constructive performance feedback and ratings to direct reports.</p>
        </div>
    </div>
</div>

<!-- Give Feedback Form Card -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-header bg-white py-3">
        <h2 class="h5 fw-bold mb-0">
            <i class="bi bi-chat-heart me-1 text-primary"></i> Give Feedback to Employee
        </h2>
    </div>
    <div class="card-body">
        <c:choose>
            <c:when test="${empty employees}">
                <div class="alert alert-info mb-0">
                    <i class="bi bi-info-circle me-1"></i>
                    No team members available. <a href="${pageContext.request.contextPath}/manager/teams" class="alert-link">Add employees to a team</a> first to provide feedback.
                </div>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/manager/feedback/create">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>

                    <div class="row g-3">
                        <div class="col-12 col-md-4">
                            <label for="fbEmployee" class="form-label fw-semibold">Employee <span class="text-danger">*</span></label>
                            <select id="fbEmployee" name="employeeId" class="form-select" required>
                                <option value="">Select Employee...</option>
                                <c:forEach var="emp" items="${employees}">
                                    <option value="${emp.id}" ${formEmployeeId == emp.id ? 'selected' : ''}>
                                        <c:out value="${emp.fullName}"/> (<c:out value="${emp.email}"/>)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-12 col-md-4">
                            <label for="fbEval" class="form-label fw-semibold">Linked Evaluation (Optional)</label>
                            <select id="fbEval" name="evaluationId" class="form-select">
                                <option value="">Not linked to an evaluation</option>
                                <c:forEach var="ev" items="${evaluations}">
                                    <option value="${ev.id}" ${formEvaluationId == ev.id ? 'selected' : ''}>
                                        <c:out value="${people[ev.employeeId] != null ? people[ev.employeeId].fullName : 'Employee'}"/> &mdash; <c:out value="${ev.periodLabel}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-12 col-md-4">
                            <label for="fbRating" class="form-label fw-semibold">Rating (1 to 5) <span class="text-danger">*</span></label>
                            <select id="fbRating" name="rating" class="form-select" required>
                                <option value="">Select Rating...</option>
                                <option value="1" ${formRating == '1' ? 'selected' : ''}>1 - Launchpad</option>
                                <option value="2" ${formRating == '2' ? 'selected' : ''}>2 - Liftoff</option>
                                <option value="3" ${formRating == '3' ? 'selected' : ''}>3 - Orbit</option>
                                <option value="4" ${formRating == '4' ? 'selected' : ''}>4 - Cruise</option>
                                <option value="5" ${formRating == '5' ? 'selected' : ''}>5 - Summit</option>
                            </select>
                        </div>

                        <div class="col-12">
                            <label for="fbComments" class="form-label fw-semibold">Feedback Comments <span class="text-danger">*</span></label>
                            <textarea id="fbComments" name="comments" class="form-control" rows="3"
                                      maxlength="1000" placeholder="Provide constructive observations, key accomplishments, or guidance for development..." required><c:out value='${formComments}'/></textarea>
                            <div class="form-text">Max 1000 characters.</div>
                        </div>

                        <div class="col-12 text-end">
                            <button type="submit" class="btn btn-primary px-4">
                                <i class="bi bi-send me-1"></i> Submit Feedback
                            </button>
                        </div>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Feedback Given Roster Card -->
<div class="card epes-card shadow-sm">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <h2 class="h5 fw-bold mb-0">Feedback Given Roster</h2>
        <span class="badge bg-secondary-subtle text-secondary border">
            <c:out value="${feedbackList.size()}"/> Records
        </span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="ps-3">Employee</th>
                        <th scope="col">Rating</th>
                        <th scope="col">Comments</th>
                        <th scope="col">Linked Period</th>
                        <th scope="col">Date</th>
                        <th scope="col" class="text-end pe-3">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty feedbackList}">
                            <tr>
                                <td colspan="6" class="text-center py-5 text-muted">
                                    <i class="bi bi-chat-square-text fs-1 d-block mb-2 text-secondary"></i>
                                    <p class="mb-1 fw-semibold">No feedback given yet.</p>
                                    <p class="small text-muted mb-0">Use the form above to share your first feedback with an employee.</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="fb" items="${feedbackList}">
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
                                    <td class="small text-muted">
                                        <c:out value="${epesFmt:formatDateTime(fb.createdAt)}"/>
                                    </td>
                                    <td class="text-end pe-3">
                                        <form method="post" action="${pageContext.request.contextPath}/manager/feedback/delete"
                                              onsubmit="return confirm('Are you sure you want to delete this feedback?');" class="d-inline">
                                            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                            <input type="hidden" name="id" value="${fb.id}"/>
                                            <button type="submit" class="btn btn-outline-danger btn-sm">
                                                <i class="bi bi-trash"></i>
                                            </button>
                                        </form>
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
