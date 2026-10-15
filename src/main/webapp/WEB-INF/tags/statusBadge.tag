<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="status" required="true" type="java.lang.Object" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty status}">
    <c:set var="statusName" value="${status.name()}" />
    <c:set var="cssClass" value="${statusName.toLowerCase().replace('_', '-')}" />
    <span class="badge status-${cssClass}"><c:out value="${statusName.replace('_', ' ')}"/></span>
</c:if>
