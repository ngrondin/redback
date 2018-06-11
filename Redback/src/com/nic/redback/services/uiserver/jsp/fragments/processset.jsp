<rb-processset 
	<% 
if(config.get('basefilter') != null) { %>
	rb-base-filter="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.UIServer.convertFilter(config.getObject('basefilter')))%>"<% 
} 
if(config.get('viewmap') != null) { %>
	rb-view-map="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.UIServer.convertFilter(config.getObject('viewmap')))%>"<% 
} 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}%>>
	#content#
</rb-processset>