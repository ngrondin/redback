<md-input-container 
	class="md-block rb-input-container">
	<label><%=config.getString('label')%></label>
	<md-icon class="md-hue-3" ><%=(config.get("icon") == null ? "today" : config.getString("icon")) %></md-icon>
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