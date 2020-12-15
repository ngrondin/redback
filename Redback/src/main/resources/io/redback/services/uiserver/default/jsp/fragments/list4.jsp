<rb-list4 <%
if(config.view != null) { %>
	[view]="'<%=config.view%>'" <%
}
if(typeof parents.dataset != 'undefined') { %>
	[dataset]="<%=parents.dataset%>" <%
}
if(config.mainattribute != null) { %>
	[mainattribute]="'<%=config.mainattribute%>'" <%
}
if(config.subattribute != null) { %>
	[subattribute]="'<%=config.subattribute%>'" <%
}
if(config.meta1attribute != null) { %>
	[meta1attribute]="'<%=config.meta1attribute%>'" <%
}
if(config.meta2attribute != null) { %>
	[meta2attribute]="'<%=config.meta2attribute%>'" <%
} %>>
</rb-list4>
