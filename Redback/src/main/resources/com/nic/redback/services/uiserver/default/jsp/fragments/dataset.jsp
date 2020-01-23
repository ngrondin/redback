<rb-dataset 
	#<%=id%>="dataset" <% 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}
if(config.get('object') != null) { %>
	[object]="'<%=config.getString('object')%>'"<% 
}
if(config.getObject('master') != null) { %>
	[relatedFilter]="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.impl.RedbackUIServer.convertFilter(config.getObject('master').containsKey('relationship') ? config.getObject('master').getObject('relationship') : config.getObject('master'))).replace('{{', '[').replace('}}', ']')%>" 
	[relatedObject]="<%=dataset%>.selectedObject"<% 
} 
if(config.get('basefilter') != null) { %>
	[baseFilter]="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.impl.RedbackUIServer.convertFilter(config.getObject('basefilter')))%>"<% 
} %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-dataset>
