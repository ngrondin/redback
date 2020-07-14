<rb-map
	#<%=id%>="rbMap" <%
if(typeof parents.datasetgroup != 'undefined' && config.series != null) { %>
	[lists]="<%=parents.datasetgroup%>.lists"
	[series]="<%=utils.convertDataEntityToAttributeString(config.series)%>"
	[selectedObject]="<%=parents.datasetgroup%>.selectedObject"
	(selectObject)="<%=parents.datasetgroup%>.select($event)" <%
} else if(typeof parents.dataset != 'undefined') { %>
	[list]="<%=parents.dataset%>.list"	
	[selectedObject]="<%=parents.dataset%>.selectedObject"
	[geoattribute]="'<%=config.geoattribute%>'"
	[labelattribute]="'<%=config.labelattribute%>'"
	[descriptionattribute]="'<%=config.descriptionattribute%>'"
	(selectObject)="<%=parents.dataset%>.select($event)" <%
} %>
	(navigate)="navigateTo($event)">
</rb-map>
