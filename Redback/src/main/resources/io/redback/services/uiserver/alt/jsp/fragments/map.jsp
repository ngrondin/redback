<rb-map
	#<%=id%>="rbMap" <%
if(typeof datasetgroup != 'undefined' && config.get('series') != null) { %>
	[lists]="<%=datasetgroup%>.lists"
	[series]="<%=utils.convertDataEntityToAttributeString(config.getList('series'))%>"
	[selectedObject]="<%=datasetgroup%>.selectedObject"
	(selectObject)="<%=datasetgroup%>.select($event)" <%
} else if(typeof dataset != 'undefined') { %>
	[list]="<%=dataset%>.list"	
	[selectedObject]="<%=dataset%>.selectedObject"
	[geoattribute]="'<%=config.getString('geoattribute')%>'"
	[labelattribute]="'<%=config.getString('labelattribute')%>'"
	[descriptionattribute]="'<%=config.getString('descriptionattribute')%>'"
	(selectObject)="<%=dataset%>.select($event)" <%
} %>
	(navigate)="navigateTo($event)">
</rb-map>