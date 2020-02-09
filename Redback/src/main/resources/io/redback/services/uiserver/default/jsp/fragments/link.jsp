<rb-link <%
if(config.get('view') != null) { %>
	[view]="'<%=config.getString('view')%>'" <%
}
if(typeof dataset != 'undefined') { %>
	[object]="<%=dataset%>.selectedObject"
	[attribute]="'<%=config.getString('attribute')%>'" <%
} %>
	(navigate)="navigateTo($event)" >
</rb-link>
