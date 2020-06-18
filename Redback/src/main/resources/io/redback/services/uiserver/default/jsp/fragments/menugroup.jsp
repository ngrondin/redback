<button
	mat-list-item  
	class="rb-menu-group md-hue-2"
	(click)="<%=menu%>.toggleGroup('<%=config.name%>')"><%
if(config.icon.indexOf(':') >= 0) {%>
	<mat-icon
		class="rb-menu-group-icon" 
		svgIcon="<%=config.icon%>">
	</mat-icon><%
} else {%>
	<mat-icon
		class="rb-menu-group-icon">
		<%=config.icon%>
	</mat-icon><%
}%>
	<span 
		flex 
		class="rb-menu-group-text"
		*ngIf="<%=menu%>.isLarge">
		<%=config.label%>
	</span>
	<mat-icon 
		*ngIf="<%=menu%>.isLarge  &&  !<%=menu%>.isGroupOpen('<%=config.name%>')">
		expand_more
	</mat-icon>
	<mat-icon 
		*ngIf="<%=menu%>.isLarge  &&  <%=menu%>.isGroupOpen('<%=config.name%>')">
		expand_less
	</mat-icon>
</button>
<mat-divider></mat-divider>
#content#
<mat-divider 
	*ngIf="<%=menu%>.isGroupOpen('<%=config.name%>')">
</mat-divider>
