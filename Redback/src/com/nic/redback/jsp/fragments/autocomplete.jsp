<md-autocomplete 
	cflex="" 
	md-input-name="autocomplete<%=config.getString('attribute')%>"  
	md-items="item in loadRelatedObjectList('<%=config.getString('attribute')%>', dynamicSearchText<%=config.getString('attribute')%>)" 
	md-selected-item="object.related.<%=config.getString('attribute')%>" 
	md-selected-item-change="object.relatedObjectHasChanged('<%=config.getString('attribute')%>')" 
	md-search-text="dynamicSearchText<%=config.getString('attribute')%>" 
	md-item-text="item.data.<%=config.getString('displayattribute')%>" 
	md-floating-label="<%=config.getString('label')%>" 
	ng-disabled="<%
			if(canWrite) {		
				%>!object.validation.<%=config.getString('attribute')%>.editable<%
			} else {
				%>true<%
			}			
			%>">
	<md-item-template><%=config.getString('listexpression')%></md-item-template>
</md-autocomplete>