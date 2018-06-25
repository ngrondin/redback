<md-input-container 
	class="md-block rb-input-container"
	style="<%=config.getString('inlineStyle')%>"
	ng-hide="!(<%=config.getString('show')%>)">
	<label><%=config.getString('label')%></label><%
if(config.get('icon') == null) {%>
	<md-icon class="md-hue-3" >description</md-icon><%	
} else if(config.getString('icon').indexOf(':') >= 0) {%>
	<md-icon class="md-hue-3" md-svg-icon="<%=config.getString('icon')%>"></md-icon><%
} else {%>
	<md-icon class="md-hue-3"><%=config.getString('icon')%></md-icon><%
}%>	
	<textarea 
		rows="3"
		rb-input
		ng-model="inputValue"
		rb-attribute="<%=config.getString('attribute')%>" 
		ng-disabled="<%
				if(canWrite) {		
					%>!object.validation.<%=config.getString('attribute')%>.editable<%
				} else {
					%>true<%
				}			
				%>">
	</textarea>
</md-input-container>