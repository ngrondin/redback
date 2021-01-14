<rb-duration-input
	class="rb-input-margin"
	style="<%=config.inlineStyle%>"
	[show]="'<%=rbutils.encode(config.show)%>'"
	[label]="'<%=config.label%>'"<%
if(config.icon == null) {%>
	[icon]="'timelapse'"<%	
} else if(config.icon.indexOf(':') >= 0) {%>
	[icon]="'<%=config.icon%>'"<%
} else {%>
	[icon]="'<%=config.icon%>'"<%
}
if(config.size != null) { %>	
	[size]="<%=(config.size == null ? 20 : config.size) %>" <%
} 
if(parents.dataset != null) { %>	
	[dataset]="<%=parents.dataset%>" <%
} 
if(config.attribute != null) { %>	
	[attribute]="'<%=(config.attribute == null ? 20 : config.attribute) %>'" <%
}%>	
	[editable]="<%=canWrite%>">
</rb-duration-input>
