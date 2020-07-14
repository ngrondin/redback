<button
	mat-list-item 
	class="rb-menu-link" <%
if(typeof parents.menugroup != 'undefined') { %>
	*ngIf="<%=parents.menu%>.isGroupOpen('<%=config.group%>')" <%
} %>	
	(click)="navigateTo({view:'<%=config.view%>', filter:{}, reset:true})"><%
if(config.icon.indexOf(':') >= 0) {%>
	<mat-icon 
		svgIcon="<%=config.icon%>"
		class="rb-menu-link-icon">
	</mat-icon><%
} else {%>
	<mat-icon
		class="rb-menu-link-icon">
		<%=config.icon%>
	</mat-icon><%
}%>
	<span 
		flex
		class="rb-menu-link-text"
		*ngIf="<%=parents.menu%>.isLarge">
		<%=config.label%>
	</span>
</button> <%
if(config.group == null) { %>
<mat-divider></mat-divider> <%
} %>
