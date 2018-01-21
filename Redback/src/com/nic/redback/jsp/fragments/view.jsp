<div 
	ng-controller="<%=config.getString('controller')%>" 
	class="rb-controller" 
<% if(config.getString('object') != null) { %>
	rb-object="<%=config.getString('object')%>"
<% } %>
<% if(config.getObject('master') != null) { %>
	rb-related="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(config.getObject('master'))%>"
<% } %>
<% if(config.getString('initialfilter') != null) { %>
	rb-initial-filter="<%=config.getString('initialfilter')%>"
<% } %>
	style="<%=config.getString('inlineStyle')%>">
	#content#
</div>