<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="epesFmt" uri="http://epes.com/tags/format" %>
<%@ taglib prefix="epes" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Goal Management — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h1 class="h2 fw-bold mb-1">Goal Management</h1>
            <p class="text-muted mb-0">Assign SMART goals to team members, track progress, and monitor deadlines.</p>
        </div>
    </div>
</div>

<!-- Assign Goal Form Card -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-header bg-white py-3">
        <h2 class="h5 fw-bold mb-0">
            <i class="bi bi-flag me-1 text-primary"></i> Assign Goal to Employee
        </h2>
    </div>
    <div class="card-body">
        <c:choose>
            <c:when test="${empty employees}">
                <div class="alert alert-info mb-0">
                    <i class="bi bi-info-circle me-1"></i>
                    No team members available. <a href="${pageContext.request.contextPath}/manager/teams" class="alert-link">Add employees to a team</a> first to assign goals.
                </div>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/manager/goals/assign">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>

                    <div class="row g-3">
                        <div class="col-12 col-md-4">
                            <label for="assignEmployee" class="form-label fw-semibold">Employee <span class="text-danger">*</span></label>
                            <select id="assignEmployee" name="employeeId" class="form-select" required>
                                <option value="">Select Employee...</option>
                                <c:forEach var="emp" items="${employees}">
                                    <option value="${emp.id}" ${formEmployeeId == emp.id ? 'selected' : ''}>
                                        <c:out value="${emp.fullName}"/> (<c:out value="${emp.email}"/>)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-12 col-md-4">
                            <label for="assignTargetDate" class="form-label fw-semibold">Target Completion Date <span class="text-danger">*</span></label>
                            <input type="date" id="assignTargetDate" name="targetDate" class="form-control"
                                   min="<c:out value='${today}'/>" required
                                   value="<c:out value='${formTargetDate}'/>"/>
                        </div>

                        <div class="col-12 col-md-4 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary w-100">
                                <i class="bi bi-plus-circle me-1"></i> Assign Goal
                            </button>
                        </div>

                        <div class="col-12">
                            <label for="assignDescription" class="form-label fw-semibold">Goal Description <span class="text-danger">*</span></label>
                            <textarea id="assignDescription" name="description" class="form-control" rows="2"
                                      maxlength="500" placeholder="e.g. Complete Docker and Kubernetes certification by Q4" required><c:out value='${formDescription}'/></textarea>
                            <div class="form-text">Clearly define the deliverable and expected impact (max 500 characters).</div>
                        </div>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Assigned Goals List Card -->
<div class="card epes-card shadow-sm">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <h2 class="h5 fw-bold mb-0">Assigned Goals Roster</h2>
        <span class="badge bg-secondary-subtle text-secondary border">
            <c:out value="${goals.size()}"/> Goals
        </span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="ps-3">Employee</th>
                        <th scope="col">Description</th>
                        <th scope="col">Target Date</th>
                        <th scope="col">Status</th>
                        <th scope="col" style="min-width: 180px;">Progress</th>
                        <th scope="col">Created</th>
                        <th scope="col" class="text-end pe-3">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty goals}">
                            <tr>
                                <td colspan="7" class="text-center py-5 text-muted">
                                    <i class="bi bi-flag fs-1 d-block mb-2 text-secondary"></i>
                                    <p class="mb-1 fw-semibold">No goals assigned yet.</p>
                                    <p class="small text-muted mb-0">Assign a new goal using the form above.</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="goal" items="${goals}">
                                <c:set var="isOverdue" value="${goal.targetDate.isBefore(today) and goal.status != 'COMPLETED'}"/>
                                <tr>
                                    <td class="ps-3 fw-semibold">
                                        <c:out value="${people[goal.employeeId] != null ? people[goal.employeeId].fullName : 'Unknown user'}"/>
                                    </td>
                                    <td>
                                        <span class="text-dark"><c:out value="${goal.description}"/></span>
                                    </td>
                                    <td>
                                        <span class="${isOverdue ? 'text-danger fw-bold' : ''}">
                                            <c:out value="${epesFmt:formatDate(goal.targetDate)}"/>
                                        </span>
                                        <c:if test="${isOverdue}">
                                            <span class="badge bg-danger ms-1">Overdue</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <epes:statusBadge status="${goal.status}"/>
                                    </td>
                                    <td>
                                        <!-- Progress Bar & Update Form -->
                                        <div class="mb-1 d-flex align-items-center gap-2">
                                            <div class="progress flex-grow-1" style="height: 8px;">
                                                <div class="progress-bar ${goal.progressPercent == 100 ? 'bg-success' : 'bg-primary'}"
                                                     role="progressbar" style="width: ${goal.progressPercent}%;"
                                                     aria-valuenow="${goal.progressPercent}" aria-valuemin="0" aria-valuemax="100"></div>
                                            </div>
                                            <span class="small fw-semibold text-muted">${goal.progressPercent}%</span>
                                        </div>
                                        <form method="post" action="${pageContext.request.contextPath}/manager/goals/progress" class="d-flex align-items-center gap-1">
                                            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                            <input type="hidden" name="id" value="${goal.id}"/>
                                            <input type="number" name="progress" min="0" max="100" value="${goal.progressPercent}"
                                                   class="form-control form-control-sm" style="width: 70px;" required/>
                                            <button type="submit" class="btn btn-sm btn-outline-secondary py-0 px-2" title="Update progress">
                                                Update
                                            </button>
                                        </form>
                                    </td>
                                    <td class="small text-muted">
                                        <c:out value="${epesFmt:formatDate(goal.createdAt)}"/>
                                    </td>
                                    <td class="text-end pe-3">
                                        <div class="btn-group btn-group-sm">
                                            <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#editGoalModal_${goal.id}">
                                                <i class="bi bi-pencil"></i> Edit
                                            </button>
                                            <form method="post" action="${pageContext.request.contextPath}/manager/goals/delete"
                                                  onsubmit="return confirm('Are you sure you want to delete this goal?');" class="d-inline">
                                                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                                <input type="hidden" name="id" value="${goal.id}"/>
                                                <button type="submit" class="btn btn-outline-danger btn-sm">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </form>
                                        </div>

                                        <!-- Edit Modal -->
                                        <div class="modal fade text-start" id="editGoalModal_${goal.id}" tabindex="-1" aria-labelledby="editModalLabel_${goal.id}" aria-hidden="true">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <form method="post" action="${pageContext.request.contextPath}/manager/goals/edit">
                                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                                        <input type="hidden" name="id" value="${goal.id}"/>
                                                        <div class="modal-header">
                                                            <h5 class="modal-title fw-bold" id="editModalLabel_${goal.id}">Edit Goal</h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                        </div>
                                                        <div class="modal-body">
                                                            <div class="mb-3">
                                                                <label class="form-label fw-semibold">Employee</label>
                                                                <input type="text" class="form-control" disabled
                                                                       value="<c:out value='${people[goal.employeeId] != null ? people[goal.employeeId].fullName : \"Unknown user\"}'/>"/>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label fw-semibold">Description <span class="text-danger">*</span></label>
                                                                <textarea name="description" class="form-control" rows="3" maxlength="500" required><c:out value='${goal.description}'/></textarea>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label fw-semibold">Target Completion Date <span class="text-danger">*</span></label>
                                                                <input type="date" name="targetDate" class="form-control" value="<c:out value='${goal.targetDate}'/>" required/>
                                                            </div>
                                                        </div>
                                                        <div class="modal-footer">
                                                            <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                            <button type="submit" class="btn btn-primary btn-sm">Save Changes</button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
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
