<rb-processset 
	<% 
if(config.get('basefilter') != null) { %>
	rb-base-filter="<%=io.redback.utils.StringUtils.convertJSONToAttributeString(io.redback.services.impl.RedbackUIServer.convertFilter(config.getObject('basefilter')))%>"<% 
} 
if(config.get('viewmap') != null) { %>
	rb-view-map="<%=io.redback.utils.StringUtils.convertJSONToAttributeString(io.redback.services.impl.RedbackUIServer.convertFilter(config.getObject('viewmap')))%>"<% 
} 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}%>>
	#content#
</rb-processset>