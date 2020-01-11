<button
	mat-list-item 
	class="rb-menu-link"
	*ngIf="<%=menu%>.isGroupOpen(<%=config.getString('group')%>)"
	(click)="navigateTo('<%=config.getString('view')%>', '<%=config.getString('label')%>')"><%
if(config.getString('icon').indexOf(':') >= 0) {%>
	<mat-icon 
		svgIcon="<%=config.getString('icon')%>"
		class="rb-menu-link-icon">
	</mat-icon><%
} else {%>
	<mat-icon
		class="rb-menu-link-icon">
		<%=config.getString('icon')%>
	</mat-icon><%
}%>
	<span 
		flex
		class="rb-menu-link-text"
		*ngIf="<%=menu%>.isLarge">
		<%=config.getString('label')%>
	</span>
</button>
