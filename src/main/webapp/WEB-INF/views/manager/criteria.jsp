<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Evaluation Criteria — EPES" />
<%@ include file="../fragments/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom gap-2">
    <div>
        <h1 class="h2 fw-bold mb-1">Evaluation Criteria</h1>
        <p class="text-muted mb-0">Define weighted evaluation rubrics used for team performance reviews.</p>
    </div>
    <a href="${pageContext.request.contextPath}/manager/dashboard" class="btn btn-outline-secondary btn-sm">
        <i class="bi bi-arrow-left me-1"></i> Back to Dashboard
    </a>
</div>

<!-- Notice Alert -->
<div class="alert alert-info d-flex align-items-center mb-4 shadow-sm" role="alert">
    <i class="bi bi-info-circle-fill fs-5 me-3 flex-shrink-0"></i>
    <div>
        <strong>Snapshot Guarantee:</strong> Criteria are copied into each evaluation as a snapshot at evaluation time. Editing or deactivating criteria later does not alter historical evaluations or reports.
    </div>
</div>

<!-- Total Active Weight Summary -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
            <h2 class="h6 fw-bold text-uppercase text-secondary mb-1">Total Active Criteria Weight</h2>
            <div class="fs-4 fw-bold ${totalActiveWeight == 100 ? 'text-success' : (totalActiveWeight > 100 ? 'text-danger' : 'text-primary')}">
                <c:out value="${totalActiveWeight}"/>% / 100%
            </div>
            <div class="small text-muted">
                <c:choose>
                    <c:when test="${totalActiveWeight == 100}">
                        <i class="bi bi-check-circle-fill text-success me-1"></i> Active weights perfectly balance to 100%.
                    </c:when>
                    <c:when test="${totalActiveWeight < 100}">
                        <i class="bi bi-exclamation-triangle text-warning me-1"></i> Total active weight is less than 100% (currently ${totalActiveWeight}%).
                    </c:when>
                    <c:otherwise>
                        <i class="bi bi-exclamation-circle text-danger me-1"></i> Total active weight exceeds 100% (currently ${totalActiveWeight}%).
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div style="min-width: 200px; max-width: 320px;" class="w-100">
            <div class="progress" style="height: 12px;" role="progressbar" aria-valuenow="${totalActiveWeight}" aria-valuemin="0" aria-valuemax="100">
                <div class="progress-bar ${totalActiveWeight == 100 ? 'bg-success' : (totalActiveWeight > 100 ? 'bg-danger' : 'bg-primary')}"
                     style="width: ${totalActiveWeight > 100 ? 100 : totalActiveWeight}%"></div>
            </div>
        </div>
    </div>
</div>

<!-- Create Criterion Card -->
<div class="card epes-card shadow-sm mb-4">
    <div class="card-header bg-white border-bottom py-3">
        <h2 class="h5 fw-bold mb-0">
            <i class="bi bi-plus-circle text-primary me-2"></i>Add New Criterion
        </h2>
    </div>
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/manager/criteria/save" class="row g-3">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>

            <div class="col-12 col-md-4">
                <label for="createName" class="form-label small fw-semibold">Criterion Name <span class="text-danger">*</span></label>
                <input type="text" class="form-control form-control-sm" id="createName" name="name"
                       placeholder="e.g. Code Quality, Problem Solving" required maxlength="100">
            </div>

            <div class="col-12 col-md-5">
                <label for="createDescription" class="form-label small fw-semibold">Description</label>
                <input type="text" class="form-control form-control-sm" id="createDescription" name="description"
                       placeholder="Description and assessment guidelines">
            </div>

            <div class="col-12 col-md-2">
                <label for="createWeight" class="form-label small fw-semibold">Weight (1-100) <span class="text-danger">*</span></label>
                <input type="number" class="form-control form-control-sm" id="createWeight" name="weight"
                       min="1" max="100" placeholder="e.g. 25" required>
            </div>

            <div class="col-12 col-md-1 d-flex align-items-end">
                <button type="submit" class="btn btn-primary btn-sm w-100" aria-label="Save Criterion">
                    <i class="bi bi-save me-1"></i> Save
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Criteria List Card -->
<div class="card epes-card shadow-sm">
    <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
        <h2 class="h5 fw-bold mb-0">
            <i class="bi bi-list-check text-primary me-2"></i>Configured Criteria
        </h2>
        <span class="badge bg-light text-dark border">
            Total: <c:out value="${criteria != null ? criteria.size() : 0}"/>
        </span>
    </div>
    <c:choose>
        <c:when test="${empty criteria}">
            <div class="card-body text-center py-5">
                <div class="fs-1 text-muted mb-3"><i class="bi bi-sliders"></i></div>
                <h3 class="h5 fw-semibold text-secondary">No criteria yet. Add your first criterion.</h3>
                <p class="text-muted small">Use the form above to configure evaluation rubrics for your team.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th scope="col" style="width: 25%">Name</th>
                            <th scope="col" style="width: 35%">Description</th>
                            <th scope="col" style="width: 10%">Weight</th>
                            <th scope="col" style="width: 10%">Status</th>
                            <th scope="col" style="width: 20%" class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${criteria}" var="c">
                            <tr class="${!c.active ? 'text-muted bg-light' : ''}">
                                <td>
                                    <span class="fw-semibold"><c:out value="${c.name}"/></span>
                                </td>
                                <td class="small">
                                    <c:out value="${not empty c.description ? c.description : '—'}"/>
                                </td>
                                <td>
                                    <span class="badge bg-info-subtle text-info-emphasis border border-info-subtle fs-6">
                                        <c:out value="${c.weight}"/>%
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${c.active}">
                                            <span class="badge bg-success">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Inactive</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <div class="d-inline-flex gap-1">
                                        <!-- Edit Modal Trigger -->
                                        <button type="button" class="btn btn-sm btn-outline-primary"
                                                data-bs-toggle="modal" data-bs-target="#editCriterionModal-${c.id}"
                                                aria-label="Edit criterion ${c.name}">
                                            <i class="bi bi-pencil me-1"></i> Edit
                                        </button>

                                        <!-- Deactivate (if active) -->
                                        <c:if test="${c.active}">
                                            <form method="post" action="${pageContext.request.contextPath}/manager/criteria/deactivate" class="d-inline m-0">
                                                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                                <input type="hidden" name="id" value="<c:out value='${c.id}'/>"/>
                                                <button type="submit" class="btn btn-sm btn-outline-secondary" aria-label="Deactivate criterion ${c.name}">
                                                    Deactivate
                                                </button>
                                            </form>
                                        </c:if>

                                        <!-- Delete -->
                                        <form method="post" action="${pageContext.request.contextPath}/manager/criteria/delete"
                                              class="d-inline m-0"
                                              onsubmit="return confirm('Are you sure you want to permanently delete this criterion?');">
                                            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                            <input type="hidden" name="id" value="<c:out value='${c.id}'/>"/>
                                            <button type="submit" class="btn btn-sm btn-outline-danger" aria-label="Delete criterion ${c.name}">
                                                <i class="bi bi-trash"></i>
                                            </button>
                                        </form>
                                    </div>

                                    <!-- Edit Modal for Criterion -->
                                    <div class="modal fade" id="editCriterionModal-${c.id}" tabindex="-1"
                                         aria-labelledby="editModalLabel-${c.id}" aria-hidden="true">
                                        <div class="modal-dialog">
                                            <div class="modal-content text-start">
                                                <form method="post" action="${pageContext.request.contextPath}/manager/criteria/save">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title fw-bold" id="editModalLabel-${c.id}">Edit Criterion</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>"/>
                                                        <input type="hidden" name="id" value="<c:out value='${c.id}'/>"/>

                                                        <div class="mb-3">
                                                            <label for="editName-${c.id}" class="form-label small fw-semibold">Criterion Name <span class="text-danger">*</span></label>
                                                            <input type="text" class="form-control form-control-sm" id="editName-${c.id}"
                                                                   name="name" value="<c:out value='${c.name}'/>" required maxlength="100">
                                                        </div>

                                                        <div class="mb-3">
                                                            <label for="editDesc-${c.id}" class="form-label small fw-semibold">Description</label>
                                                            <textarea class="form-control form-control-sm" id="editDesc-${c.id}"
                                                                      name="description" rows="3"><c:out value="${c.description}"/></textarea>
                                                        </div>

                                                        <div class="mb-3">
                                                            <label for="editWeight-${c.id}" class="form-label small fw-semibold">Weight (1-100) <span class="text-danger">*</span></label>
                                                            <input type="number" class="form-control form-control-sm" id="editWeight-${c.id}"
                                                                   name="weight" value="<c:out value='${c.weight}'/>" min="1" max="100" required>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                        <button type="submit" class="btn btn-sm btn-primary">Save Changes</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
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
