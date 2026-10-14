<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Admin Dashboard — EPES" />
<%@ include file="fragments/header.jspf" %>

<div class="mb-4 pb-2 border-bottom">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
            <h1 class="h2 fw-bold mb-1">
                Welcome, <c:out value="${user.fullName}"/>
            </h1>
            <p class="text-muted mb-0">
                <span class="badge bg-danger me-2">ADMINISTRATOR</span>
                <span>System Administration & Governance</span>
            </p>
        </div>
    </div>
</div>

<div class="row g-4">
    <!-- User Management Card (Enabled) -->
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card h-100 p-3 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-primary">
                        <i class="bi bi-people-fill"></i>
                    </div>
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                        <c:out value="${totalUsers}"/> Total
                    </span>
                </div>
                <h2 class="h5 fw-bold card-title">Users Management</h2>
                <p class="card-text text-muted small mb-3">
                    Manage system users, activate/deactivate accounts, assign roles, and view global directory.
                </p>
                <div class="small text-muted bg-light p-2 rounded mb-2">
                    <div class="d-flex justify-content-between mb-1">
                        <span>Admins: <strong><c:out value="${adminCount}"/></strong></span>
                        <span>Managers: <strong><c:out value="${managerCount}"/></strong></span>
                        <span>Employees: <strong><c:out value="${employeeCount}"/></strong></span>
                    </div>
                    <div class="d-flex justify-content-between text-secondary">
                        <span>Active: <span class="text-success fw-semibold"><c:out value="${activeCount}"/></span></span>
                        <span>Inactive: <span class="text-danger fw-semibold"><c:out value="${inactiveCount}"/></span></span>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-sm btn-primary w-100">
                    <i class="bi bi-arrow-right-circle me-1"></i> Manage Users
                </a>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
