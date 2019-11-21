<mat-list-item 
	class="rb-menu-group md-hue-2"
	(click)="#<%=menu#%>.togglegroup(<%=config.getString('_id')%>)"><%
if(config.getString('icon').indexOf(':') >= 0) {%>
	<mat-icon md-svg-icon="<%=config.getString('icon')%>"></mat-icon><%
} else {%>
	<mat-icon><%=config.getString('icon')%></mat-icon><%
}%>
	<span 
		flex 
		*ngIf="largemenu">
		<%=config.getString('label')%>
	</span>
	<mat-icon 
		*ngIf="largemenu  &&  !#<%=menu#%>.isGroupOpen(<%=config.getString('_id')%>)">
		expand_more
	</mat-icon>
	<mat-icon 
		*ngIf="largemenu  &&  #<%=menu#%>.isGroupOpen(<%=config.getString('_id')%>)">
		expand_less
	</mat-icon>
</mat-list-item>
<mat-divider></mat-divider>
#content#
<mat-divider 
	*ngIf="!#<%=menu#%>.isGroupOpen(<%=config.getString('_id')%>)">
</mat-divider>
