<rb-map
	#<%=id%>="rbMap" <%
if(typeof parents.datasetgroup != 'undefined' && config.series != null) { %>
	[datasetgroup]="<%=parents.datasetgroup%>"
	[series]="<%=utils.convertDataEntityToAttributeString(config.series)%>"
	[selectedObject]="<%=parents.datasetgroup%>.selectedObject"
	(selectObject)="<%=parents.datasetgroup%>.select($event)" <%
} else if(typeof parents.dataset != 'undefined') { %>
	[dataset]="<%=parents.dataset%>"	
	[geoattribute]="'<%=config.geoattribute%>'"
	[labelattribute]="'<%=config.labelattribute%>'"
	[descriptionattribute]="'<%=config.descriptionattribute%>'"
	(selectObject)="<%=parents.dataset%>.select($event)" <%
} %>
	(navigate)="navigateTo($event)">
</rb-map>
