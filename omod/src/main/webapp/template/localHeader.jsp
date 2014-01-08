<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/import") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/shr-cdahandler/import.form"><spring:message
				code="org.openmrs.module.cda-handler.import" /></a>
	</li>
</ul>
<h2>
	<spring:message code="${project.parent.artifactId}.title" />
</h2>
