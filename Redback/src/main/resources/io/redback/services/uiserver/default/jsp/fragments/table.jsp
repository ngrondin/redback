<rb-table <%
if(typeof parents.dataset != 'undefined') { %>
	[list]="<%=parents.dataset%>.list"
	[dataset]="<%=parents.dataset%>" <%
} %>	
	[columns]="<%=utils.convertDataEntityToAttributeString(config.columns)%>">
</rb-table>
