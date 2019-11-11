<rb-form 
	<% 
if(config.get('object') != null) { %>
	rb-object="<%=config.getString('object')%>"<% 
}
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}%>>
	#content#
</rb-form>