<div 
	ng-controller="<%=config.getString('controller')%>" 
	class="rb-controller" 
<% if(config.get('object') != null) { %>
	rb-object="<%=config.getString('object')%>"
<% } %>
<% if(config.getObject('master') != null) { %>
	rb-related="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(config.getObject('master'))%>"
<% } %>
<% if(config.get('initialfilter') != null) { %>
	rb-initial-filter="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(config.getObject('initialfilter'))%>"
<% } %>
	style="<%=config.getString('inlineStyle')%>">
	#content#
</div>