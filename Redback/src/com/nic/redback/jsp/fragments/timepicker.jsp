<md-input-container 
	class="md-block" >
	<label><%=config.getString('label')%></label>
	<md-icon class="md-hue-3" >description</md-icon>
	<input 
		type="time"
		ng-model="object.data.<%=config.getString('attribute')%>" 
		ng-change="object.attributeHasChanged('<%=config.getString('attribute')%>')" 
		ng-disabled="<%
				if(canWrite) {		
					%>!object.validation.<%=config.getString('attribute')%>.editable<%
				} else {
					%>true<%
				}			
				%>">
</md-input-container>