<rb-processset 
	<% 
if(config.basefilter != null) { %>
	rb-base-filter="<%=io.redback.utils.StringUtils.convertJSONToAttributeString(io.redback.services.impl.RedbackUIServer.convertFilter(config.basefilter))%>"<% 
} 
if(config.viewmap != null) { %>
	rb-view-map="<%=io.redback.utils.StringUtils.convertJSONToAttributeString(io.redback.services.impl.RedbackUIServer.convertFilter(config.viewmap))%>"<% 
} 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}%>>
	#content#
</rb-processset>
