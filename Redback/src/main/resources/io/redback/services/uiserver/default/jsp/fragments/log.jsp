<rb-log
	style="<%=config.inlineStyle%>"
	[show]="'<%=rbutils.encode(config.show)%>'"<%
if(config.size != null) { %>	
	[size]="<%=(config.size == null ? 20 : config.size) %>" <%
} 
if(typeof parents.dataset != 'undefined') { %>	
	[dataset]="<%=parents.dataset%>" <%
} 
if(config.userattribute != null) { %>	
	[userattribute]="'<%=config.userattribute%>'" <%
} 
if(config.dateattribute != null) { %>	
	[dateattribute]="'<%=config.dateattribute%>'" <%
} 
if(config.entryattribute != null) { %>	
	[entryattribute]="'<%=config.entryattribute%>'" <%
} 
if(config.categoryattribute != null) { %>	
	[categoryattribute]="'<%=config.categoryattribute%>'" <%
} %>
	[editable]="<%=canWrite%>" <%
if(typeof parents.dataset != 'undefined') { %>
	(posted)="<%=parents.dataset%>.action('create', $event)" <%
} %> >
</rb-log>
