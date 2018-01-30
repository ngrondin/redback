<md-input-container 
	class="md-block"
	style="<%=config.getString('inlineStyle')%>" >
	<label><%=config.getString('label')%></label>
	<md-icon class="md-hue-3" >description</md-icon>
	<textarea 
		rows="3"
		ng-model="object.data.<%=config.getString('attribute')%>" 
		ng-change="object.attributeHasChanged('<%=config.getString('attribute')%>')" 
		ng-disabled="<%
				if(canWrite) {		
					%>!object.validation.<%=config.getString('attribute')%>.editable<%
				} else {
					%>true<%
				}			
				%>">
	</textarea>
</md-input-container>