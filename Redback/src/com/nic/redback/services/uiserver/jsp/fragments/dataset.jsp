<rb-dataset 
	<% 
if(config.get('object') != null) { %>
	rb-object="<%=config.getString('object')%>"<% 
}
if(config.getObject('master') != null) { %>
	rb-related="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.UIServer.convertFilter(config.getObject('master')))%>"<% 
} 
if(config.get('basefilter') != null) { %>
	rb-base-filter="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.UIServer.convertFilter(config.getObject('basefilter')))%>"<% 
} 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}%>>
	#content#
</rb-dataset>