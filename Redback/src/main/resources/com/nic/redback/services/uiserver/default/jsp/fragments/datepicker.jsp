<md-input-container 
	class="md-block rb-input-container">
	<label><%=config.getString('label')%></label><%
if(config.get('icon') == null) {%>
	<md-icon class="md-hue-3" >today</md-icon><%	
} else if(config.getString('icon').indexOf(':') >= 0) {%>
	<md-icon class="md-hue-3" md-svg-icon="<%=config.getString('icon')%>"></md-icon><%
} else {%>
	<md-icon class="md-hue-3"><%=config.getString('icon')%></md-icon><%
}%>
	<input
		rb-datetime-input
		ng-model="formattedDateTime"  
		rb-attribute="<%=config.getString('attribute')%>"
  		rb-format="<%=(config.get("format") == null ? "YYYY-MM-DD HH:mm" : config.getString("format")) %>"
  		ng-disabled="<%
				if(canWrite) {		
					%>!object.validation.<%=config.getString('attribute')%>.editable<%
				} else {
					%>true<%
				}			
				%>">
	</input>
</md-input-container>