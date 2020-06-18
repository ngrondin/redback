<rb-dataset 
	#<%=id%>="dataset" <% 
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
if(typeof dataset == 'undefined') { %>
	[(userFilter)]="currentTarget.filter" 
	[(searchString)]="currentTarget.search" 
	[(selectedObject)]="currentTarget.selectedObject" <%
} else if(config.master != null) { %>
	[relatedFilter]="<%=utils.convertDataMapToAttributeString(config.master.relationship)%>" 
	[relatedObject]="<%=dataset%>.selectedObject"<% 
}  
if(typeof datasetgroup != 'undefined' && config.name != null) { %>
	(initiated)="<%=datasetgroup%>.register('<%=config.name%>', <%=id%>)" <%
} %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-dataset>
