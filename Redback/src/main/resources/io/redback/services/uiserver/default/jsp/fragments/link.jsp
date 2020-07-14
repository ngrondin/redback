<rb-link <%
if(config.view != null) { %>
	[view]="'<%=config.view%>'" <%
}
if(typeof parents.dataset != 'undefined') { %>
	[object]="<%=parents.dataset%>.selectedObject"
	[attribute]="'<%=config.attribute%>'" <%
} %>
	(navigate)="navigateTo($event)" >
</rb-link>
