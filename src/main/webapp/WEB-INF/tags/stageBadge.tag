<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="stage" required="true" type="java.lang.Object" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty stage}">
    <span class="badge stage-${stage.name().toLowerCase()} badge-stage-${stage.name().toLowerCase()}"><c:out value="${stage.label != null ? stage.label : stage}"/></span>
</c:if>
