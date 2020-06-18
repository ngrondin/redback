<rb-link <%
if(config.view != null) { %>
	[view]="'<%=config.view%>'" <%
}
if(typeof dataset != 'undefined') { %>
	[object]="<%=dataset%>.selectedObject"
	[attribute]="'<%=config.attribute%>'" <%
} %>
	(navigate)="navigateTo($event)" >
</rb-link>
