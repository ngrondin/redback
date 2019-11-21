<mat-list-item 
	class="rb-menu-link"
	*ngIf="menutogglegroup<%=config.getString('group')%>"
	(click)="navigate('<%=config.getString('view')%>', '<%=config.getString('label')%>')"><%
if(config.getString('icon').indexOf(':') >= 0) {%>
	<mat-icon mat-svg-icon="<%=config.getString('icon')%>"></mat-icon><%
} else {%>
	<mat-icon><%=config.getString('icon')%></mat-icon><%
}%>
	<span 
		*ngIf="largemenu" 
		flex>
		<%=config.getString('label')%>
	</span>
</mat-list-item>
