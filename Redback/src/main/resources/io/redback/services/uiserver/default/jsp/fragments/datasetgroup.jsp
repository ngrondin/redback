<rb-datasetgroup 
	#<%=id%> <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
} 
if(typeof parents.tab != 'undefined') { %>
	[activator]="<%=parents.tab%>" <%
} %>>
	#content#
</rb-datasetgroup>
