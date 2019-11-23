<rb-dataset 
	#<%=id%>="dataset" <% 
if(config.get('object') != null) { %>
	[object]="<%=config.getString('object')%>"<% 
}
if(config.getObject('master') != null) { %>
	[relatedFilter]="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.impl.RedbackUIServer.convertFilter(config.getObject('master')))%>"<% 
} 
if(config.get('basefilter') != null) { %>
	[baseFilter]="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.impl.RedbackUIServer.convertFilter(config.getObject('basefilter')))%>"<% 
} 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}%>>
	#content#
</rb-dataset>