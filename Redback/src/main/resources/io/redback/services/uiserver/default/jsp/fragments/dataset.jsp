<rb-dataset 
	#<%=id%>="dataset" <% 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}
if(config.get('object') != null) { %>
	[object]="'<%=config.getString('object')%>'"<% 
}
if(config.get('basefilter') != null) { %>
	[baseFilter]="<%=utils.convertDataMapToAttributeString(config.getObject('basefilter'))%>"<% 
} 
if(typeof dataset == 'undefined') { %>
	[(userFilter)]="currentTarget.filter" 
	[(searchString)]="currentTarget.search" 
	[(selectedObject)]="currentTarget.selectedObject" <%
} else if(config.getObject('master') != null) { %>
	[relatedFilter]="<%=utils.convertDataMapToAttributeString(config.getObject('master').getObject('relationship'))%>" 
	[relatedObject]="<%=dataset%>.selectedObject"<% 
}  %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-dataset>
