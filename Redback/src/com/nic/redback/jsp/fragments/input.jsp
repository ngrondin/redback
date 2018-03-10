<md-input-container 
	class="md-block rb-input-container" 
	style="<%=config.getString('inlineStyle')%>"
	ng-hide="!(<%=config.getString('show')%>)">
	<label><%=config.getString('label')%></label>
	<md-icon class="md-hue-3" ><%=(config.get("icon") == null ? "description" : config.getString("icon")) %></md-icon>
	<input 
		rb-input
		ng-model="inputValue"
		rb-attribute="<%=config.getString('attribute')%>" 
		size="<%=(config.get("size") == null ? "" : config.getString("size")) %>"
		ng-disabled="<%
				if(canWrite) {		
					%>!object.validation.<%=config.getString('attribute')%>.editable<%
				} else {
					%>true<%
				}			
				%>">
</md-input-container>