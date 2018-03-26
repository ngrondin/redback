<md-switch 
	ng-model="object.data.<%=config.getString('attribute')%>" 
	aria-label="Switch 6" 
	class="md-primary" 
	md-no-ink="" 
	ng-change="object.attributeHasChanged('<%=config.getString('attribute')%>')" >
	<%=config.getString('label')%>
</md-switch>