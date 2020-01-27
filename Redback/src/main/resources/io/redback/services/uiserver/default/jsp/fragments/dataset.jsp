<rb-dataset 
	#<%=id%>="dataset" <% 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}
if(config.get('object') != null) { %>
	[object]="'<%=config.getString('object')%>'"<% 
}
if(config.getObject('master') != null) { %>
	[relatedFilter]="<%=utils.convertDataMapToAttributeString(utils.convertFilterForClient(config.getObject('master').containsKey('relationship') ? config.getObject('master').getObject('relationship') : config.getObject('master'))).replace('{{', '[').replace('}}', ']')%>" 
	[relatedObject]="<%=dataset%>.selectedObject"<% 
} 
if(config.get('basefilter') != null) { %>
	[baseFilter]="<%=utils.convertDataMapToAttributeString(utils.convertFilterForClient(config.getObject('basefilter')))%>"<% 
} 
if(typeof dataset == 'undefined') { %>
	[initialUserFilter]="initialUserFilter" <%
} %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-dataset>
