<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="User Management — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom gap-2">
    <div>
        <h1 class="h2 fw-bold mb-1">User Management</h1>
        <p class="text-muted mb-0">Create, monitor, and configure role assignments across the organization.</p>
    </div>
    <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-secondary btn-sm">
        <i class="bi bi-arrow-left me-1"></i> Back to Dashboard
    </a>
</div>

<!-- Create User Form Card -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-header bg-white border-bottom py-3">
        <h2 class="h5 fw-bold mb-0">
            <i class="bi bi-person-plus-fill text-primary me-2"></i>Create New User
        </h2>
    </div>
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/admin/users/create" class="row g-3">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>

            <div class="col-12 col-md-4">
                <label for="createEmail" class="form-label small fw-semibold">Email Address <span class="text-danger">*</span></label>
                <input type="email" class="form-control form-control-sm" id="createEmail" name="email"
                       placeholder="user@epes.local" value="<c:out value='${formEmail}'/>" required>
            </div>

            <div class="col-12 col-md-4">
                <label for="createFullName" class="form-label small fw-semibold">Full Name <span class="text-danger">*</span></label>
                <input type="text" class="form-control form-control-sm" id="createFullName" name="fullName"
                       placeholder="Full Display Name" value="<c:out value='${formFullName}'/>" required>
            </div>

            <div class="col-12 col-md-3">
                <label for="createRole" class="form-label small fw-semibold">System Role <span class="text-danger">*</span></label>
                <select class="form-select form-select-sm" id="createRole" name="role" required>
                    <option value="" disabled ${empty formRole ? 'selected' : ''}>Select Role...</option>
                    <option value="ADMIN" ${formRole == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                    <option value="MANAGER" ${formRole == 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                    <option value="EMPLOYEE" ${formRole == 'EMPLOYEE' ? 'selected' : ''}>EMPLOYEE</option>
                </select>
            </div>

            <div class="col-12 col-md-1 d-flex align-items-end">
                <button type="submit" class="btn btn-primary btn-sm w-100" aria-label="Create User Account">
                    <i class="bi bi-plus-lg me-1"></i> Create
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Users Table Card -->
<div class="card epes-card shadow-sm">
    <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
        <h2 class="h5 fw-bold mb-0">
            <i class="bi bi-people-fill text-primary me-2"></i>Registered System Users
        </h2>
        <span class="badge bg-light text-dark border">
            Total: <c:out value="${users != null ? users.size() : 0}"/>
        </span>
    </div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
                <tr>
                    <th scope="col" style="width: 5%">ID</th>
                    <th scope="col" style="width: 25%">Name &amp; Email</th>
                    <th scope="col" style="width: 12%">Role</th>
                    <th scope="col" style="width: 10%">Status</th>
                    <th scope="col" style="width: 15%">Created Date</th>
                    <th scope="col" style="width: 20%">Change Role</th>
                    <th scope="col" style="width: 13%">Account Status</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${users}" var="u">
                    <c:set var="isSelf" value="${u.id == sessionScope.currentUser.id}" />
                    <tr class="${isSelf ? 'table-info-subtle' : ''}">
                        <td><c:out value="${u.id}"/></td>
                        <td>
                            <div class="fw-semibold"><c:out value="${u.fullName}"/></div>
                            <div class="text-muted small"><c:out value="${u.email}"/></div>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${u.role == 'ADMIN'}">
                                    <span class="badge bg-danger">ADMIN</span>
                                </c:when>
                                <c:when test="${u.role == 'MANAGER'}">
                                    <span class="badge bg-primary">MANAGER</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-success">EMPLOYEE</span>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${isSelf}">
                                <span class="badge bg-secondary ms-1">You</span>
                            </c:if>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${u.active}">
                                    <span class="badge bg-success">Active</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Inactive</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="small text-muted">
                            <c:out value="${u.createdAt != null ? u.createdAt.toLocalDate() : '-'}"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${isSelf}">
                                    <div class="d-flex align-items-center gap-1" title="You cannot change your own role" data-bs-toggle="tooltip">
                                        <select class="form-select form-select-sm" disabled style="max-width: 130px;" aria-label="Change role disabled">
                                            <option><c:out value="${u.role}"/></option>
                                        </select>
                                        <button class="btn btn-sm btn-outline-secondary" disabled title="You cannot change your own role" aria-label="Save role disabled">Save</button>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/users/role" class="d-flex align-items-center gap-1 m-0">
                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                        <input type="hidden" name="userId" value="<c:out value='${u.id}'/>"/>
                                        <select name="role" class="form-select form-select-sm" style="max-width: 130px;" aria-label="Select new role for ${u.fullName}">
                                            <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                                            <option value="MANAGER" ${u.role == 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                                            <option value="EMPLOYEE" ${u.role == 'EMPLOYEE' ? 'selected' : ''}>EMPLOYEE</option>
                                        </select>
                                        <button type="submit" class="btn btn-sm btn-outline-primary" aria-label="Save role for ${u.fullName}">Save</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${isSelf}">
                                    <button class="btn btn-sm btn-outline-secondary w-100" disabled
                                            title="You cannot deactivate your own account" data-bs-toggle="tooltip"
                                            aria-label="Deactivate self disabled">
                                        Deactivate
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/users/active" class="m-0">
                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                        <input type="hidden" name="userId" value="<c:out value='${u.id}'/>"/>
                                        <input type="hidden" name="active" value="${!u.active}"/>
                                        <c:choose>
                                            <c:when test="${u.active}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger w-100" aria-label="Deactivate account for ${u.fullName}">
                                                    <i class="bi bi-person-x me-1"></i> Deactivate
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="submit" class="btn btn-sm btn-outline-success w-100" aria-label="Activate account for ${u.fullName}">
                                                    <i class="bi bi-person-check me-1"></i> Activate
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <c:if test="${!hasOtherUsers}">
        <div class="card-body text-center py-4 border-top">
            <p class="text-muted mb-0">No other users besides your administrator account yet. Use the form above to add users.</p>
        </div>
    </c:if>
</div>

<%@ include file="../fragments/footer.jspf" %>
