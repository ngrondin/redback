<rb-link <%
if(config.view != null) { %>
	[view]="'<%=config.view%>'" <%
}
if(typeof parents.dataset != 'undefined') { %>
	[dataset]="<%=parents.dataset%>"
	[attribute]="'<%=config.attribute%>'" <%
} %>
	(navigate)="navigateTo($event)" >
</rb-link>
