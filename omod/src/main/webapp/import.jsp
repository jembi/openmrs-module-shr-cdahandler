<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<form id="importForm" modelAttribute="document" method="post" enctype="multipart/form-data">
		<table>
			<tr>
				<td>Select File:</td>
				<td><input type="file" name="importFile"/></td>
			</tr>
		</table>
		<br/>
		<input type="hidden" name="tempFile" value="${document.tempFile }"/>
		
		<input name="action" type="submit" value="Upload">
		<hr/>

<c:choose>
	<c:when test="${document.html != null}">
		<div>
		${document.html}
		</div>
	</c:when>
	<c:when test="${document.title != null}">
		<table border="1" style="width:100%">
			<caption>Importing ${document.title }</caption>
			<c:forEach var="section" items="${document.sections}">
				<tr>
					<td><input type="checkbox" name="section" value="${section.code.code}" checked="checked"/></td>
					<td> ${section.title }</td>
				</tr>
			</c:forEach>
		</table>
		<input type="submit" name="action" value="Import"/>
	</c:when>
	<c:otherwise>
		<input type="hidden" name="section" value="${document.title }"/>
	</c:otherwise>
</c:choose>
</form>


<%@ include file="/WEB-INF/template/footer.jsp"%>
