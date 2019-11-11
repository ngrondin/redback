<md-input-container 
	class="md-block"
	ng-hide="!(<%=config.getString('show')%>)">
	<label><%=config.getString('label')%></label>
	<md-icon class="md-hue-3"> list </md-icon>
	<md-select 
		ng-model="object.related.<%=config.getString('attribute')%>" 
		md-on-open="loadRelatedObjectList('<%=config.getString('attribute')%>', null)" 
		ng-change="object.relatedObjectHasChanged('<%=config.getString('attribute')%>')" 
		ng-disabled="<%
				if(canWrite) {		
					%>!object.validation.<%=config.getString('attribute')%>.editable<%
				} else {
					%>true<%
				}			
				%>">
		<md-option 
			ng-repeat="item in object.validation.<%=config.getString('attribute')%>.listofvalues" 
			ng-value="item">{{item.data.<%=config.getString('displayattribute')%>}}</md-option>
	</md-select>
</md-input-container>