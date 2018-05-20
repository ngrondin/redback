<div 
	ng-controller="form" 
	class="rb-controller" <% 
if(config.get('object') != null) { %>
	rb-object="<%=config.getString('object')%>"<% 
} %>
	style="<%=config.getString('inlineStyle')%>">
	#content#
</div>