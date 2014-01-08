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
<input type="submit" value="Upload and Import">
<br/>
<div>
${document.html}
</div>
</form>


<%@ include file="/WEB-INF/template/footer.jsp"%>
