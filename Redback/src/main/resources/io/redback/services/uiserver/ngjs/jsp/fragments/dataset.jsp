<rb-dataset 
	<% 
if(config.get('object') != null) { %>
	rb-object="<%=config.getString('object')%>"<% 
}
if(config.getObject('master') != null) { %>
	rb-related="<%=utils.convertDataMapToAttributeString(utils.convertFilterForClient(config.getObject('master')))%>"<% 
} 
if(config.get('basefilter') != null) { %>
	rb-base-filter="<%=utils.convertDataMapToAttributeString(utils.convertFilterForClient(config.getObject('basefilter')))%>"<% 
} 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}%>>
	#content#
</rb-dataset>