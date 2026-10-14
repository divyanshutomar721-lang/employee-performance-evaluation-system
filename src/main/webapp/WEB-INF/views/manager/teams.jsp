<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Teams — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom gap-2">
    <div>
        <h1 class="h2 fw-bold mb-1">Team Management</h1>
        <p class="text-muted mb-0">Organize and manage team rosters and member assignments.</p>
    </div>
    <a href="${pageContext.request.contextPath}/manager/dashboard" class="btn btn-outline-secondary btn-sm">
        <i class="bi bi-arrow-left me-1"></i> Back to Dashboard
    </a>
</div>

<!-- Create Team Form Card -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-header bg-white border-bottom py-3">
        <h2 class="h5 fw-bold mb-0">
            <i class="bi bi-diagram-3-fill text-primary me-2"></i>Create New Team
        </h2>
    </div>
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/manager/teams/create">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>

            <div class="row g-3 mb-3">
                <div class="col-12 col-md-5">
                    <label for="teamName" class="form-label small fw-semibold">Team Name <span class="text-danger">*</span></label>
                    <input type="text" class="form-control form-control-sm" id="teamName" name="name"
                           placeholder="e.g. Core Engineering, UX Design" required maxlength="150">
                </div>
                <div class="col-12 col-md-7">
                    <label for="teamDesc" class="form-label small fw-semibold">Description</label>
                    <input type="text" class="form-control form-control-sm" id="teamDesc" name="description"
                           placeholder="Team mission and responsibilities">
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label small fw-semibold d-block">
                    Select Team Members
                    <span class="text-muted fw-normal">(Optional initial membership)</span>
                </label>

                <c:choose>
                    <c:when test="${empty assignableEmployees}">
                        <div class="alert alert-warning py-2 mb-0 small">
                            <i class="bi bi-exclamation-triangle me-1"></i>
                            No active employees available to assign. Ask an admin to create employee accounts.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="border rounded p-3 bg-light" style="max-height: 220px; overflow-y: auto;">
                            <div class="row g-2">
                                <c:forEach items="${assignableEmployees}" var="emp">
                                    <div class="col-12 col-md-6">
                                        <div class="card card-body p-2 border bg-white h-100">
                                            <div class="form-check mb-1">
                                                <input class="form-check-input" type="checkbox" name="employeeIds"
                                                       value="<c:out value='${emp.id}'/>" id="empCheck-${emp.id}">
                                                <label class="form-check-label fw-semibold small" for="empCheck-${emp.id}">
                                                    <c:out value="${emp.fullName}"/>
                                                    <span class="text-muted fw-normal">(<c:out value="${emp.email}"/>)</span>
                                                </label>
                                            </div>
                                            <div class="input-group input-group-sm mt-1">
                                                <span class="input-group-text small">Role</span>
                                                <input type="text" name="role_<c:out value='${emp.id}'/>"
                                                       class="form-control form-control-sm"
                                                       value="Member" placeholder="e.g. Member, Lead, Analyst"
                                                       aria-label="Role in team for ${emp.fullName}">
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="d-flex justify-content-end">
                <button type="submit" class="btn btn-primary btn-sm px-4">
                    <i class="bi bi-plus-lg me-1"></i> Create Team
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Teams List Card -->
<div class="card epes-card shadow-sm">
    <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
        <h2 class="h5 fw-bold mb-0">
            <i class="bi bi-collection text-primary me-2"></i>My Managed Teams
        </h2>
        <span class="badge bg-light text-dark border">
            Total: <c:out value="${teams != null ? teams.size() : 0}"/>
        </span>
    </div>

    <c:choose>
        <c:when test="${empty teams}">
            <div class="card-body text-center py-5">
                <div class="fs-1 text-muted mb-3"><i class="bi bi-people"></i></div>
                <h3 class="h5 fw-semibold text-secondary">No teams yet. Create your first team.</h3>
                <p class="text-muted small">Use the form above to initialize a new team with member roles.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th scope="col" style="width: 25%">Team Name</th>
                            <th scope="col" style="width: 45%">Description</th>
                            <th scope="col" style="width: 15%">Members</th>
                            <th scope="col" style="width: 15%" class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${teams}" var="t">
                            <tr>
                                <td>
                                    <span class="fw-bold"><c:out value="${t.name}"/></span>
                                </td>
                                <td class="small text-muted">
                                    <c:out value="${not empty t.description ? t.description : '—'}"/>
                                </td>
                                <td>
                                    <span class="badge bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle fs-6">
                                        <i class="bi bi-person-fill me-1"></i>
                                        <c:out value="${t.members != null ? t.members.size() : 0}"/>
                                    </span>
                                </td>
                                <td class="text-end">
                                    <a href="${pageContext.request.contextPath}/manager/teams/view?id=<c:out value='${t.id}'/>"
                                       class="btn btn-sm btn-outline-primary">
                                        <i class="bi bi-folder2-open me-1"></i> Open
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="../fragments/footer.jspf" %>
