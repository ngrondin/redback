<rb-aggregateset 
	#<%=id%>="aggregateset" <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(config.object != null) { %>
	[object]="'<%=config.object%>'"<% 
}
if(config.basefilter != null) { %>
	[baseFilter]="<%=utils.convertDataMapToAttributeString(config.basefilter)%>"<% 
}
if(config.tuple != null) { %>
	[tuple]="<%=utils.convertDataEntityToAttributeString(config.tuple)%>"<% 
} 
if(config.metrics != null) { %>
	[metrics]="<%=utils.convertDataEntityToAttributeString(config.metrics)%>"<% 
} 
if(typeof dataset == 'undefined') { %>
	[(userFilter)]="currentTarget.filter" 
	[(searchString)]="currentTarget.search" 
	[(selectedObject)]="currentTarget.selectedObject" <%
} else if(config.master != null) { %>
	[relatedFilter]="<%=utils.convertDataMapToAttributeString(config.master.relationship)%>" 
	[relatedObject]="<%=dataset%>.selectedObject"<% 
} %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-aggregateset>
