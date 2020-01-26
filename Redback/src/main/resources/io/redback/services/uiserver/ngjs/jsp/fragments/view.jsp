<div 
	ng-controller="<%=config.getString('controller')%>" 
	class="rb-controller" 
<% if(config.get('object') != null) { %>
	rb-object="<%=config.getString('object')%>"
<% } %>
<% if(config.getObject('master') != null) { %>
	rb-related="<%=utils.convertDataMapToAttributeString(config.getObject('master'))%>"
<% } %>
<% if(config.get('basefilter') != null) { %>
	rb-base-filter="<%=utils.convertDataMapToAttributeString(config.getObject('basefilter'))%>"
<% } %>
	style="<%=config.getString('inlineStyle')%>">
	#content#
</div>