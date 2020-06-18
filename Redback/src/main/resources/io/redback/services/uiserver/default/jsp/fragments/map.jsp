<rb-map
	#<%=id%>="rbMap" <%
if(typeof datasetgroup != 'undefined' && config.series != null) { %>
	[lists]="<%=datasetgroup%>.lists"
	[series]="<%=utils.convertDataEntityToAttributeString(config.series)%>"
	[selectedObject]="<%=datasetgroup%>.selectedObject"
	(selectObject)="<%=datasetgroup%>.select($event)" <%
} else if(typeof dataset != 'undefined') { %>
	[list]="<%=dataset%>.list"	
	[selectedObject]="<%=dataset%>.selectedObject"
	[geoattribute]="'<%=config.geoattribute%>'"
	[labelattribute]="'<%=config.labelattribute%>'"
	[descriptionattribute]="'<%=config.descriptionattribute%>'"
	(selectObject)="<%=dataset%>.select($event)" <%
} %>
	(navigate)="navigateTo($event)">
</rb-map>
