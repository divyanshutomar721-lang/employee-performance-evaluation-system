<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${team.name} — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom gap-2">
    <div>
        <div class="d-flex align-items-center gap-2 mb-1">
            <h1 class="h2 fw-bold mb-0"><c:out value="${team.name}"/></h1>
            <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                <i class="bi bi-people-fill me-1"></i> <c:out value="${members != null ? members.size() : 0}"/> Members
            </span>
        </div>
        <p class="text-muted mb-0"><c:out value="${not empty team.description ? team.description : 'No description provided.'}"/></p>
    </div>
    <div class="d-flex gap-2">
        <a href="${pageContext.request.contextPath}/manager/teams" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-arrow-left me-1"></i> All Teams
        </a>
        <form method="post" action="${pageContext.request.contextPath}/manager/teams/delete" class="d-inline m-0"
              onsubmit="return confirm('Are you sure you want to permanently delete this team? All member assignments will be removed.');">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
            <input type="hidden" name="teamId" value="<c:out value='${team.id}'/>"/>
            <button type="submit" class="btn btn-outline-danger btn-sm" aria-label="Delete Team">
                <i class="bi bi-trash me-1"></i> Delete Team
            </button>
        </form>
    </div>
</div>

<div class="row g-4">
    <!-- Left Column: Edit Details & Add Member -->
    <div class="col-12 col-lg-4">
        <!-- Edit Team Details Card -->
        <div class="card epes-card shadow-sm mb-4">
            <div class="card-header bg-white border-bottom py-3">
                <h2 class="h6 fw-bold mb-0">
                    <i class="bi bi-pencil-square text-primary me-2"></i>Edit Team Details
                </h2>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/manager/teams/update">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                    <input type="hidden" name="teamId" value="<c:out value='${team.id}'/>"/>

                    <div class="mb-3">
                        <label for="editTeamName" class="form-label small fw-semibold">Team Name <span class="text-danger">*</span></label>
                        <input type="text" class="form-control form-control-sm" id="editTeamName" name="name"
                               value="<c:out value='${team.name}'/>" required maxlength="150">
                    </div>

                    <div class="mb-3">
                        <label for="editTeamDesc" class="form-label small fw-semibold">Description</label>
                        <textarea class="form-control form-control-sm" id="editTeamDesc" name="description"
                                  rows="3"><c:out value="${team.description}"/></textarea>
                    </div>

                    <button type="submit" class="btn btn-primary btn-sm w-100">
                        <i class="bi bi-check-lg me-1"></i> Save Changes
                    </button>
                </form>
            </div>
        </div>

        <!-- Add Member Form Card -->
        <div class="card epes-card shadow-sm">
            <div class="card-header bg-white border-bottom py-3">
                <h2 class="h6 fw-bold mb-0">
                    <i class="bi bi-person-plus text-success me-2"></i>Add Member
                </h2>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty availableToAdd}">
                        <p class="text-muted small mb-0">
                            <i class="bi bi-info-circle me-1"></i>
                            All assignable employees are already members of this team.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <form method="post" action="${pageContext.request.contextPath}/manager/teams/members/add">
                            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                            <input type="hidden" name="teamId" value="<c:out value='${team.id}'/>"/>

                            <div class="mb-3">
                                <label for="employeeSelect" class="form-label small fw-semibold">Employee <span class="text-danger">*</span></label>
                                <select class="form-select form-select-sm" id="employeeSelect" name="employeeId" required>
                                    <option value="" disabled selected>Select employee...</option>
                                    <c:forEach items="${availableToAdd}" var="cand">
                                        <option value="<c:out value='${cand.id}'/>">
                                            <c:out value="${cand.fullName}"/> (<c:out value="${cand.email}"/>)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="mb-3">
                                <label for="newMemberRole" class="form-label small fw-semibold">Role in Team</label>
                                <input type="text" class="form-control form-control-sm" id="newMemberRole" name="roleInTeam"
                                       value="Member" placeholder="e.g. Lead, Frontend Developer, QA">
                            </div>

                            <button type="submit" class="btn btn-success btn-sm w-100">
                                <i class="bi bi-plus-lg me-1"></i> Add to Team
                            </button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Right Column: Members Table -->
    <div class="col-12 col-lg-8">
        <div class="card epes-card shadow-sm">
            <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-people-fill text-primary me-2"></i>Team Members
                </h2>
                <span class="badge bg-light text-dark border">
                    <c:out value="${members != null ? members.size() : 0}"/> Assigned
                </span>
            </div>

            <c:choose>
                <c:when test="${empty members}">
                    <div class="card-body text-center py-5">
                        <div class="fs-1 text-muted mb-3"><i class="bi bi-person-x"></i></div>
                        <h3 class="h6 fw-semibold text-secondary">No members currently in this team.</h3>
                        <p class="text-muted small">Use the "Add Member" form to assign employees to this team.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th scope="col" style="width: 35%">Name &amp; Email</th>
                                    <th scope="col" style="width: 35%">Role in Team</th>
                                    <th scope="col" style="width: 15%">Joined Date</th>
                                    <th scope="col" style="width: 15%" class="text-end">Remove</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${members}" var="m">
                                    <c:set var="uSummary" value="${userSummaries[m.employeeId]}"/>
                                    <tr>
                                        <td>
                                            <div class="fw-semibold">
                                                <c:out value="${uSummary != null ? uSummary.fullName : ('Employee #' += m.employeeId)}"/>
                                            </div>
                                            <div class="text-muted small">
                                                <c:out value="${uSummary != null ? uSummary.email : 'ID: ' += m.employeeId}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <form method="post" action="${pageContext.request.contextPath}/manager/teams/members/role"
                                                  class="d-flex align-items-center gap-1 m-0">
                                                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                                <input type="hidden" name="teamId" value="<c:out value='${team.id}'/>"/>
                                                <input type="hidden" name="employeeId" value="<c:out value='${m.employeeId}'/>"/>
                                                <input type="text" name="roleInTeam" class="form-control form-control-sm"
                                                       value="<c:out value='${m.roleInTeam}'/>" style="max-width: 160px;"
                                                       aria-label="Role in team for member">
                                                <button type="submit" class="btn btn-sm btn-outline-primary" aria-label="Save member role">
                                                    Save
                                                </button>
                                            </form>
                                        </td>
                                        <td class="small text-muted">
                                            <c:out value="${m.joinedAt != null ? m.joinedAt.toLocalDate() : '-'}"/>
                                        </td>
                                        <td class="text-end">
                                            <form method="post" action="${pageContext.request.contextPath}/manager/teams/members/remove"
                                                  class="d-inline m-0"
                                                  onsubmit="return confirm('Remove this member from the team?');">
                                                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                                <input type="hidden" name="teamId" value="<c:out value='${team.id}'/>"/>
                                                <input type="hidden" name="employeeId" value="<c:out value='${m.employeeId}'/>"/>
                                                <button type="submit" class="btn btn-sm btn-outline-danger" aria-label="Remove member from team">
                                                    <i class="bi bi-x-circle me-1"></i> Remove
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="../fragments/footer.jspf" %>
