<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    Integer status = (Integer) request.getAttribute("errorStatus");
    if (status != null) {
        response.setStatus(status);
    }
%>
<c:set var="pageTitle" value="Error — EPES" />
<%@ include file="fragments/header.jspf" %>

<div class="row justify-content-center py-5">
    <div class="col-12 col-md-8 col-lg-6 text-center">
        <div class="card epes-card border-0 p-5 shadow-sm">
            <div class="card-body">
                <div class="mb-3">
                    <span class="badge bg-danger fs-5 px-3 py-2">
                        <c:out value="${errorStatus != null ? errorStatus : 'Error'}"/>
                    </span>
                </div>
                <h1 class="h2 fw-bold mb-3">Something went wrong</h1>
                <p class="text-muted fs-6 mb-4">
                    <c:out value="${errorMessage != null ? errorMessage : 'An unexpected error occurred. Please try again.'}"/>
                </p>

                <div class="d-flex justify-content-center gap-3">
                    <a href="${pageContext.request.contextPath}/home" class="btn btn-primary px-4">
                        <i class="bi bi-house-door me-1"></i> Go to Dashboard
                    </a>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-secondary px-4">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Sign in
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
