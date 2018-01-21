<md-input-container 
	class="md-block">
	<label><%=config.getString('label')%></label>
	<md-datepicker  
		ng-model="object.data.<%=config.getString('attribute')%>" 
  		ng-change="object.attributeHasChanged('<%=config.getString('attribute')%>')" 
  		ng-disabled="<%
				if(canWrite) {		
					%>!object.validation.<%=config.getString('attribute')%>.editable<%
				} else {
					%>true<%
				}			
				%>">
	</md-datepicker>
</md-input-container>