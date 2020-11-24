<rb-dataset 
	#<%=id%> <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(config.object != null) { %>
	[object]="'<%=config.object%>'"
	[fetchAll]="<%=config.fetchall%>"<% 
}
if(config.basefilter != null) { %>
	[baseFilter]="<%=utils.convertDataMapToAttributeString(config.basefilter)%>"<% 
}
if(config.basesort != null) { %>
	[baseSort]="<%=utils.convertDataMapToAttributeString(config.basesort)%>"<% 
}  
if(typeof parents.dataset == 'undefined' && typeof parents.datasetgroup == 'undefined') { %>
	[(userFilter)]="currentTarget.filter" 
	[(searchString)]="currentTarget.search" 
	[(selectedObject)]="currentTarget.selectedObject" <%
} else if(config.master != null) { %>
	[relatedFilter]="<%=utils.convertDataMapToAttributeString(config.master.relationship)%>" 
	[relatedObject]="<%=parents.dataset%>.selectedObject"<% 
}  
if(typeof parents.datasetgroup != 'undefined' && config.name != null) { %>
	(initiated)="<%=parents.datasetgroup%>.register('<%=config.name%>', <%=id%>)" <%
} %>
	[active]="<%=(typeof parents.tab !== 'undefined' ? parents.tab + ".active" : "true")%>"
	(openModal)="openModal($event)">
	#content#
</rb-dataset>
