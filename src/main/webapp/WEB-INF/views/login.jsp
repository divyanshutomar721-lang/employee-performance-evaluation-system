<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign in — EPES" />
<%@ include file="fragments/header.jspf" %>

<div class="row justify-content-center">
    <div class="col-12 col-sm-10 col-md-8 col-lg-5">
        <div class="card epes-card shadow-sm border-0 p-4">
            <div class="card-body">
                <div class="text-center mb-4">
                    <div class="display-6 text-primary mb-2">
                        <i class="bi bi-person-circle"></i>
                    </div>
                    <h1 class="h3 fw-bold mb-1">Sign in to EPES</h1>
                    <p class="text-muted small">Enter your email address to access your dashboard</p>
                </div>

                <c:if test="${not empty loginError}">
                    <div class="alert alert-danger" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>
                        <c:out value="${loginError}"/>
                    </div>
                </c:if>

                <c:if test="${tempLoginActive}">
                    <div class="alert alert-info d-flex align-items-center mb-3" role="alert">
                        <i class="bi bi-info-circle-fill me-2 flex-shrink-0"></i>
                        <small>Temporary login: enter your registered email.</small>
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>" />

                    <div class="mb-3">
                        <label for="email" class="form-label fw-semibold">Email address</label>
                        <input type="email"
                               class="form-control"
                               id="email"
                               name="email"
                               value="<c:out value='${email}'/>"
                               placeholder="name@example.com"
                               required
                               autofocus />
                    </div>

                    <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Sign in
                    </button>
                </form>

                <div class="mt-4 pt-3 border-top text-center">
                    <a href="${pageContext.request.contextPath}/" class="text-decoration-none small text-muted">
                        <i class="bi bi-arrow-left me-1"></i> Back to Home
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
