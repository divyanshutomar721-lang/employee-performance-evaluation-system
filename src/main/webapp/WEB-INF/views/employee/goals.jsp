<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="My Goals — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom d-flex flex-wrap justify-content-between align-items-center gap-2">
    <div>
        <h1 class="h2 fw-bold mb-1">My Performance Goals</h1>
        <p class="text-muted mb-0">Track self-directed personal objectives and manager-assigned performance milestones.</p>
    </div>
    <div class="d-flex gap-2">
        <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#newGoalModal">
            <i class="bi bi-plus-circle me-1"></i> New Personal Goal
        </button>
        <a href="${pageContext.request.contextPath}/employee/dashboard" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i> Dashboard
        </a>
    </div>
</div>

<c:choose>
    <c:when test="${empty goals}">
        <div class="card epes-card shadow-sm p-5 text-center my-4">
            <i class="bi bi-flag fs-1 text-muted opacity-50 mb-3 d-block"></i>
            <h2 class="h5 fw-bold text-dark mb-1">No goals recorded yet.</h2>
            <p class="text-muted small mb-3">You can create self-assigned personal development goals or wait for goals assigned by your manager.</p>
            <div>
                <button type="button" class="btn btn-sm btn-primary" data-bs-toggle="modal" data-bs-target="#newGoalModal">
                    <i class="bi bi-plus-circle me-1"></i> Create Your First Goal
                </button>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row g-4">
            <c:forEach var="goal" items="${goals}">
                <c:set var="isPersonal" value="${goal.goalType == 'PERSONAL' or goal.setBy == sessionScope.currentUser.id}"/>
                <c:set var="isOverdue" value="${goal.targetDate.isBefore(today) and goal.status != 'COMPLETED'}"/>
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card epes-card shadow-sm h-100 ${isOverdue ? 'border-danger' : ''}">
                        <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                            <div>
                                <c:choose>
                                    <c:when test="${isPersonal}">
                                        <span class="badge bg-success-subtle text-success border border-success-subtle">
                                            <i class="bi bi-person me-1"></i>Personal
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                                            <i class="bi bi-briefcase me-1"></i>Assigned by <c:out value="${managers[goal.setBy] != null ? managers[goal.setBy].fullName : 'Manager'}"/>
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <epes:statusBadge status="${goal.status}"/>
                        </div>

                        <div class="card-body">
                            <p class="card-text fw-medium text-dark mb-3">
                                <c:out value="${goal.description}"/>
                            </p>

                            <div class="small text-muted mb-2">
                                <i class="bi bi-calendar-event me-1"></i> Target Date:
                                <span class="${isOverdue ? 'text-danger fw-bold' : 'fw-semibold text-dark'}">
                                    <c:out value="${epesFmt:formatDate(goal.targetDate)}"/>
                                </span>
                                <c:if test="${isOverdue}">
                                    <span class="badge bg-danger ms-1">Overdue</span>
                                </c:if>
                            </div>

                            <div class="mb-3">
                                <div class="d-flex justify-content-between align-items-center small mb-1">
                                    <span class="text-muted">Progress</span>
                                    <span class="fw-bold">${goal.progressPercent}%</span>
                                </div>
                                <div class="progress" style="height: 8px;">
                                    <div class="progress-bar ${goal.progressPercent == 100 ? 'bg-success' : 'bg-primary'}"
                                         style="width: ${goal.progressPercent}%;"></div>
                                </div>
                            </div>

                            <!-- Progress update form (on all goals) -->
                            <form method="post" action="${pageContext.request.contextPath}/employee/goals/progress" class="mb-2">
                                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                <input type="hidden" name="id" value="${goal.id}"/>
                                <div class="input-group input-group-sm">
                                    <input type="number" name="progress" min="0" max="100" class="form-control"
                                           value="${goal.progressPercent}" placeholder="0-100" required/>
                                    <button type="submit" class="btn btn-outline-primary">Update %</button>
                                </div>
                            </form>
                        </div>

                        <!-- Edit & Delete controls ONLY for Personal Goals -->
                        <c:if test="${isPersonal}">
                            <div class="card-footer bg-transparent border-top d-flex justify-content-end gap-2 py-2">
                                <button type="button" class="btn btn-sm btn-outline-secondary"
                                        data-bs-toggle="modal" data-bs-target="#editGoalModal${goal.id}">
                                    <i class="bi bi-pencil me-1"></i> Edit
                                </button>
                                <form method="post" action="${pageContext.request.contextPath}/employee/goals/delete"
                                      onsubmit="return confirm('Delete this personal goal?');" class="d-inline m-0">
                                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                    <input type="hidden" name="id" value="${goal.id}"/>
                                    <button type="submit" class="btn btn-sm btn-outline-danger">
                                        <i class="bi bi-trash me-1"></i> Delete
                                    </button>
                                </form>
                            </div>

                            <!-- Modal for Editing Personal Goal -->
                            <div class="modal fade" id="editGoalModal${goal.id}" tabindex="-1" aria-hidden="true">
                                <div class="modal-dialog">
                                    <div class="modal-content">
                                        <form method="post" action="${pageContext.request.contextPath}/employee/goals/edit">
                                            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                            <input type="hidden" name="id" value="${goal.id}"/>
                                            <div class="modal-header">
                                                <h5 class="modal-title fw-bold">Edit Personal Goal</h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                            </div>
                                            <div class="modal-body">
                                                <div class="mb-3">
                                                    <label class="form-label fw-semibold">Description</label>
                                                    <textarea name="description" class="form-control" rows="3" maxlength="500" required><c:out value="${goal.description}"/></textarea>
                                                    <div class="form-text">Max 500 characters.</div>
                                                </div>
                                                <div class="mb-3">
                                                    <label class="form-label fw-semibold">Target Date</label>
                                                    <input type="date" name="targetDate" class="form-control"
                                                           value="${goal.targetDate}" required/>
                                                </div>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                <button type="submit" class="btn btn-primary">Save Changes</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<!-- Modal for New Personal Goal -->
<div class="modal fade" id="newGoalModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form method="post" action="${pageContext.request.contextPath}/employee/goals/create">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                <div class="modal-header">
                    <h5 class="modal-title fw-bold">New Personal Goal</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="newGoalDesc" class="form-label fw-semibold">Goal Description</label>
                        <textarea id="newGoalDesc" name="description" class="form-control" rows="3" maxlength="500"
                                  placeholder="e.g. Complete AWS Certified Developer certification" required></textarea>
                        <div class="form-text">Max 500 characters. Clear and measurable.</div>
                    </div>
                    <div class="mb-3">
                        <label for="newGoalDate" class="form-label fw-semibold">Target Completion Date</label>
                        <input type="date" id="newGoalDate" name="targetDate" class="form-control"
                               min="${today}" required/>
                        <div class="form-text">Target completion date cannot be in the past.</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Create Goal</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="../fragments/footer.jspf" %>
