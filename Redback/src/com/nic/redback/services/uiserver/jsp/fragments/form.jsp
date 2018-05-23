<div 
	ng-controller="form" 
	class="rb-form" <% 
if(config.get('object') != null) { %>
	rb-object="<%=config.getString('object')%>"<% 
} %>
	style="<%=config.getString('inlineStyle')%>">
	#content#
</div>