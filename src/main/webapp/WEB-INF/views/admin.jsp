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
                <span>System Administration & Governance &mdash; Rocket Stage: <strong class="text-primary">Summit</strong></span>
            </p>
        </div>
    </div>
</div>

<div class="row g-4">
    <div class="col-12 col-md-6 col-lg-4">
        <div class="card epes-card disabled-feature h-100 p-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="fs-3 text-secondary">
                        <i class="bi bi-people-fill"></i>
                    </div>
                    <span class="badge bg-light text-muted border">Coming in Phase 4</span>
                </div>
                <h2 class="h5 fw-bold card-title">Users Management</h2>
                <p class="card-text text-muted small">
                    Manage system users, activate/deactivate accounts, assign roles, and view global directory.
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 pt-0">
                <button class="btn btn-sm btn-outline-secondary w-100 disabled" disabled>Open Users</button>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
