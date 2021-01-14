<rb-code-input
	class="rb-input-margin"
	style="<%=config.inlineStyle%>"
	[show]="'<%=rbutils.encode(config.show)%>'"<%
if(config.size != null) { %>	
	[size]="<%=config.size%>" <%
} 
if(config.mode != null) { %>	
	[mode]="'<%=config.mode%>'" <%
} else {%>	
	[mode]="'javascript'" <%
} 
if(parents.dataset != null) { %>	
	[dataset]="<%=parents.dataset%>" <%
} 
if(config.attribute != null) { %>	
	[attribute]="'<%=(config.attribute == null ? 20 : config.attribute) %>'" <%
}
if(config.rows != null) { %>	
	[rows]="<%=config.rows%>" <%
} %>	
	[editable]="<%=canWrite%>"
	>
</rb-code-input>
