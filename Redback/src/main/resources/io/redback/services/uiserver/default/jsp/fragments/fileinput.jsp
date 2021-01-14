<rb-file-input
	[show]="'<%=rbutils.encode(config.show)%>'"
	[label]="'<%=config.label%>'"<%
if(config.size != null) { %>	
	[size]="<%=(config.size == null ? 20 : config.size) %>" <%
} 
if(parents.dataset != null) { %>	
	[dataset]="<%=parents.dataset%>" <%
} 
if(config.attribute != null) { %>	
	[attribute]="'<%=(config.attribute == null ? 20 : config.attribute) %>'" <%
} %>
	[editable]="<%=canWrite%>" >
</rb-file-input>
