<button
	mat-list-item  
	class="rb-menu-group md-hue-2"
	(click)="<%=menu%>.toggleGroup('<%=config.getString('_id')%>')"><%
if(config.getString('icon').indexOf(':') >= 0) {%>
	<mat-icon md-svg-icon="<%=config.getString('icon')%>"></mat-icon><%
} else {%>
	<mat-icon><%=config.getString('icon')%></mat-icon><%
}%>
	<span 
		flex 
		class="rb-menu-group-text"
		*ngIf="<%=menu%>.isLarge">
		<%=config.getString('label')%>
	</span>
	<mat-icon 
		*ngIf="<%=menu%>.isLarge  &&  !<%=menu%>.isGroupOpen('<%=config.getString('_id')%>')">
		expand_more
	</mat-icon>
	<mat-icon 
		*ngIf="<%=menu%>.isLarge  &&  <%=menu%>.isGroupOpen('<%=config.getString('_id')%>')">
		expand_less
	</mat-icon>
</button>
<mat-divider></mat-divider>
#content#
<mat-divider 
	*ngIf="<%=menu%>.isGroupOpen('<%=config.getString('_id')%>')">
</mat-divider>
